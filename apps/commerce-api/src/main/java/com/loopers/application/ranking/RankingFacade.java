package com.loopers.application.ranking;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.ranking.dto.RankingResult;
import com.loopers.domain.ranking.MonthlyRankingMV;
import com.loopers.domain.ranking.WeeklyRankingMV;
import com.loopers.infrastructure.ranking.MonthlyRankingRepository;
import com.loopers.infrastructure.ranking.WeeklyRankingRepository;
import com.loopers.interfaces.api.ranking.ProductRankingInfo;
import com.loopers.interfaces.api.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingFacade {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductFacade productFacade;
    private final WeeklyRankingRepository weeklyRepo;
    private final MonthlyRankingRepository monthlyRepo;

    public RankingResult getRankings(PeriodType period, LocalDate targetDate, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        log.info("랭킹 조회 시작 - period={}, date={}, page={}, size={}", period, targetDate, page, size);

        RankingResult result;

        switch (period) {
            case DAILY -> {
                // Redis (오늘 → 어제 → 그제)
                result = fetchFromRedisWithFallback(targetDate, start, end);
                if (!result.isEmpty()) {
                    enrichEntriesWithProductInfo(result.getEntries());
                    return result;
                }
                // 일간이 비어있으면 그냥 empty 반환
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("redis-empty")
                    .build();
            }
            case WEEKLY -> {
                result = fetchFromWeeklyDb(targetDate, page, size);
                if (!result.isEmpty()) {
                    enrichEntriesWithProductInfo(result.getEntries());
                    return result;
                }
                // 주간 비면 empty
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-weekly-empty")
                    .build();
            }
            case MONTHLY -> {
                result = fetchFromMonthlyDb(targetDate, page, size);
                if (!result.isEmpty()) {
                    enrichEntriesWithProductInfo(result.getEntries());
                    return result;
                }
                // 월간 비면 empty
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-monthly-empty")
                    .build();
            }
            default -> {
                // 방어 로직: 혹시 모르는 값이면 일간 규칙로
                result = fetchFromRedisWithFallback(targetDate, start, end);
                if (!result.isEmpty()) {
                    enrichEntriesWithProductInfo(result.getEntries());
                    return result;
                }
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("unknown-period")
                    .build();
            }
        }
    }

    // ====== 이하 기존 헬퍼들 유지 ======

    public ProductRankingInfo getProductRanking(Long productId, LocalDate targetDate) {
        String member = "product:" + productId;
        Long rank = null;
        Double score = null;
        LocalDate actual = targetDate;

        for (int i = 0; i < 3; i++) {
            LocalDate d = targetDate.minusDays(i);
            String key = dailyKey(d);
            Double s = redisTemplate.opsForZSet().score(key, member);
            if (s != null) {
                score = s;
                Long r = redisTemplate.opsForZSet().reverseRank(key, member);
                rank = (r == null) ? null : r + 1;
                actual = d;
                break;
            }
        }

        ProductInfo productInfo = null;
        try {
            Map<Long, ProductInfo> map = productFacade.getProductInfoMap(List.of(productId));
            productInfo = map.get(productId);
        } catch (Exception e) {
            log.warn("상품 상세 조회 실패 productId={}", productId, e);
        }

        return ProductRankingInfo.builder()
            .productId(productId)
            .date(actual)
            .rank(rank)
            .score(score)
            .productInfo(productInfo)
            .build();
    }

    private RankingResult fetchFromRedisWithFallback(LocalDate targetDate, int start, int end) {
        for (int i = 0; i < 3; i++) {
            LocalDate d = targetDate.minusDays(i);
            String key = dailyKey(d);
            Set<TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            if (tuples != null && !tuples.isEmpty()) {
                List<RankingEntry> entries = tuples.stream()
                    .map(t -> RankingEntry.builder()
                        .productId(parseProductId(t.getValue()))
                        .score(t.getScore() == null ? 0.0 : t.getScore())
                        .productInfo(null)
                        .build())
                    .filter(e -> e.getProductId() != null)
                    .collect(Collectors.toList());
                String src = (i == 0) ? "redis" : (i == 1 ? "redis-fallback-yesterday" : "redis-fallback-day-before");
                return RankingResult.builder()
                    .actualDate(d)
                    .entries(entries)
                    .source(src)
                    .build();
            }
        }
        return RankingResult.builder()
            .actualDate(targetDate)
            .entries(List.of())
            .source("redis-empty")
            .build();
    }

    private RankingResult fetchFromWeeklyDb(LocalDate targetDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<WeeklyRankingMV> rows = weeklyRepo.findByTargetDateOrderByRankNoAsc(targetDate, pageable);

        LocalDate actual = targetDate;
        String source = "db-weekly";

        if (rows == null || rows.isEmpty()) {
            LocalDate latest = weeklyRepo.findLatestTargetDate(targetDate);
            if (latest == null) {
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-weekly-empty")
                    .build();
            }
            rows = weeklyRepo.findByTargetDateOrderByRankNoAsc(latest, pageable);
            actual = latest;
            source = "db-weekly-fallback";
        }

        List<RankingEntry> entries = rows.stream()
            .map(this::toEntryFromWeekly)
            .collect(Collectors.toList());

        return RankingResult.builder()
            .actualDate(actual)
            .entries(entries)
            .source(source)
            .build();
    }

    private RankingResult fetchFromMonthlyDb(LocalDate targetDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<MonthlyRankingMV> rows = monthlyRepo.findByTargetDateOrderByRankNoAsc(targetDate, pageable);

        LocalDate actual = targetDate;
        String source = "db-monthly";

        if (rows == null || rows.isEmpty()) {
            LocalDate latest = monthlyRepo.findLatestTargetDate(targetDate);
            if (latest == null) {
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-monthly-empty")
                    .build();
            }
            rows = monthlyRepo.findByTargetDateOrderByRankNoAsc(latest, pageable);
            actual = latest;
            source = "db-monthly-fallback";
        }

        List<RankingEntry> entries = rows.stream()
            .map(this::toEntryFromMonthly)
            .collect(Collectors.toList());

        return RankingResult.builder()
            .actualDate(actual)
            .entries(entries)
            .source(source)
            .build();
    }

    private RankingEntry toEntryFromWeekly(WeeklyRankingMV mv) {
        return RankingEntry.builder()
            .productId(mv.getProductId())
            .score(mv.getRankingScore()) // 필드명에 맞게
            .productInfo(null)
            .build();
    }

    private RankingEntry toEntryFromMonthly(MonthlyRankingMV mv) {
        return RankingEntry.builder()
            .productId(mv.getProductId())
            .score(mv.getRankingScore())
            .productInfo(null)
            .build();
    }

    private void enrichEntriesWithProductInfo(List<RankingEntry> entries) {
        if (entries == null || entries.isEmpty()) return;
        List<Long> ids = entries.stream()
            .map(RankingEntry::getProductId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        try {
            Map<Long, ProductInfo> infoMap = productFacade.getProductInfoMap(ids);
            for (RankingEntry e : entries) {
                e.setProductInfo(infoMap.get(e.getProductId()));
            }
        } catch (Exception ex) {
            log.warn("상품정보 배치 조회 실패", ex);
        }
    }

    private String dailyKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private Long parseProductId(String member) {
        if (member == null || !member.startsWith("product:")) return null;
        try {
            return Long.parseLong(member.substring(8));
        } catch (NumberFormatException e) {
            log.warn("잘못된 product member 형식: {}", member);
            return null;
        }
    }
}
