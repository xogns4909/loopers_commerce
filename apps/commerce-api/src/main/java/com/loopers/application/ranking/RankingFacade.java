package com.loopers.application.ranking;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.interfaces.api.ranking.ProductRankingInfo;
import com.loopers.interfaces.api.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 랭킹 관련 비즈니스 로직을 담당하는 Facade
 * Controller와 실제 랭킹 데이터 처리 로직을 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingFacade {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ProductFacade productFacade;
    
    /**
     * 페이지네이션 지원 랭킹 조회 (Fallback 전략 포함)
     */
    public RankingResult getRankings(LocalDate targetDate, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;
        
        log.info("랭킹 조회 시작 - date: {}, page: {}, size: {}", targetDate, page, size);
        
        // 1. Redis Fallback 전략 (오늘 → 어제 → 전전날)
        RankingResult fallbackResult = getRankingWithFallback(targetDate, start, end);
        
        // 2. 상품 정보 enrichment (상품명, 가격 등 추가)
        if (!fallbackResult.isEmpty()) {
            List<RankingEntry> enrichedEntries = enrichWithProductInfo(fallbackResult.entries());
            fallbackResult = fallbackResult.withEnrichedEntries(enrichedEntries);
        }
        
        log.info("랭킹 조회 완료 - 실제 날짜: {}, 결과 수: {}, 소스: {}", 
                fallbackResult.actualDate(), fallbackResult.entries().size(), fallbackResult.source());
        
        return fallbackResult;
    }
    
    /**
     * 개별 상품 랭킹 조회
     */
    public ProductRankingInfo getProductRanking(Long productId, LocalDate targetDate) {
        String key = generateRankingKey(targetDate);
        String member = "product:" + productId;
        
        Double score = redisTemplate.opsForZSet().score(key, member);
        Long rank = null;
        
        if (score != null) {
            rank = redisTemplate.opsForZSet().reverseRank(key, member);
            if (rank != null) {
                rank += 1; // 0-based → 1-based
            }
        }
        
        return ProductRankingInfo.of(productId, targetDate, rank, score);
    }
    
    /**
     * Fallback 전략으로 랭킹 데이터 조회
     */
    private RankingResult getRankingWithFallback(LocalDate targetDate, int start, int end) {
        for (int i = 0; i < 3; i++) {
            LocalDate fallbackDate = targetDate.minusDays(i);
            String key = generateRankingKey(fallbackDate);
            
            Set<TypedTuple<String>> results = 
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            
            if (results != null && !results.isEmpty()) {
                List<RankingEntry> entries = results.stream()
                    .map(tuple -> {
                        Long productId = parseProductId(tuple.getValue());
                        Double score = tuple.getScore();
                        return new RankingEntry(productId, score != null ? score : 0.0, null);
                    })
                    .filter(entry -> entry.productId() != null)
                    .collect(Collectors.toList());
                
                String source = i == 0 ? "today" : (i == 1 ? "yesterday" : "day-before-yesterday");
                return RankingResult.of(fallbackDate, entries, source);
            }
        }
        
        log.info("모든 Fallback 실패, 빈 랭킹 반환: {}", targetDate);
        return RankingResult.empty(targetDate);
    }
    
    /**
     * 상품 정보로 랭킹 엔트리 enrichment (안전한 처리)
     */
    private List<RankingEntry> enrichWithProductInfo(List<RankingEntry> entries) {
        if (entries.isEmpty()) {
            return entries;
        }
        
        List<Long> productIds = entries.stream()
            .map(RankingEntry::productId)
            .collect(Collectors.toList());
        
        try {
            // 배치로 한 번에 조회
            Map<Long, ProductInfo> productInfoMap = productFacade.getProductInfoMap(productIds);
            log.info("상품 정보 조회 결과: 요청={}, 응답={}", productIds.size(), productInfoMap.size());
            
            return entries.stream()
                .map(entry -> {
                    ProductInfo productInfo = productInfoMap.get(entry.productId());
                    if (productInfo == null) {
                        log.warn("상품 정보 누락: productId={} (랭킹에는 있지만 DB에서 조회 안됨)", entry.productId());
                    }
                    return new RankingEntry(entry.productId(), entry.score(), productInfo);
                })
                .filter(entry -> entry.productInfo() != null) // null인 항목 제거
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("상품 정보 조회 실패 - productIds: {}", productIds, e);
            
            // Graceful degradation: productInfo 없이라도 productId와 score는 제공
            return entries.stream()
                .map(entry -> new RankingEntry(entry.productId(), entry.score(), null))
                .collect(Collectors.toList());
        }
    }
    
    private String generateRankingKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    private Long parseProductId(String member) {
        if (member == null || !member.startsWith("product:")) {
            return null;
        }
        
        try {
            return Long.parseLong(member.substring(8));
        } catch (NumberFormatException e) {
            log.warn("잘못된 상품 ID 형식: {}", member);
            return null;
        }
    }
    
    /**
     * 랭킹 결과 데이터 클래스
     */
    public record RankingResult(
        LocalDate actualDate,
        List<RankingEntry> entries,
        String source
    ) {
        public static RankingResult of(LocalDate date, List<RankingEntry> entries, String source) {
            return new RankingResult(date, entries, source);
        }
        
        public static RankingResult empty(LocalDate targetDate) {
            return new RankingResult(targetDate, List.of(), "empty");
        }
        
        public boolean isEmpty() {
            return entries.isEmpty();
        }
        
        public RankingResult withEnrichedEntries(List<RankingEntry> enrichedEntries) {
            return new RankingResult(actualDate, enrichedEntries, source);
        }
    }
}
