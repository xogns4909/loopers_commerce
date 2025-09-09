package com.loopers.ranking;

import com.loopers.event.GeneralEnvelopeEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RankingProperties rankingProperties;
    private final RankingKeyGenerator keyGenerator;
    private final ScoreCalculator scoreCalculator;
    
    public void updateRanking(GeneralEnvelopeEvent event) {
        if (!rankingProperties.isEnabled()) {
            return;
        }
        
        if (!scoreCalculator.isRankingEvent(event.type())) {
            return;
        }
        
        Long productId = extractProductId(event.payload());
        if (productId == null) {
            return;
        }
        
        String member = keyGenerator.generateProductMember(productId);
        LocalDate today = LocalDate.now();
        
        // 1. 이벤트 타입별 키에 카운트 저장 (가중치 변경 시 재계산용)
        String typeKey = keyGenerator.generateDailyKey(today, event.type());
        redisTemplate.opsForZSet().incrementScore(typeKey, member, 1.0);
        redisTemplate.expire(typeKey, Duration.ofHours(rankingProperties.getTtl().getHours()));
        
        // 2. 종합 점수 키에 가중치 적용된 점수 저장 (일반 조회용)
        String totalKey = keyGenerator.generateDailyKey(today);
        double weightedScore = getWeightedScore(event.type());
        redisTemplate.opsForZSet().incrementScore(totalKey, member, weightedScore);
        redisTemplate.expire(totalKey, Duration.ofHours(rankingProperties.getTtl().getHours()));
        
        log.debug("Ranking updated - typeKey: {}, totalKey: {}, productId: {}, weightedScore: {}", 
                 typeKey, totalKey, productId, weightedScore);
    }
    
    private double getWeightedScore(String eventType) {
        var weights = rankingProperties.getScore().getWeights();
        return switch(eventType) {
            case "PRODUCT_VIEWED" -> weights.getView();
            case "PRODUCT_LIKED" -> weights.getLike();
            case "PRODUCT_UNLIKED" -> weights.getUnlike();
            case "ORDER_CREATED" -> weights.getOrder();
            default -> 0.0;
        };
    }
    
    private Long extractProductId(JsonNode payload) {
        if (payload == null || !payload.has("productId")) {
            log.warn("payload에 productId가 없습니다: {}", payload);
            return null;
        }
        return payload.get("productId").asLong();
    }
}
