package com.loopers.ranking;

import com.fasterxml.jackson.databind.JsonNode;
import com.loopers.event.EventTypes;
import com.loopers.event.GeneralEnvelopeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Redis 가중치 관리 기반 랭킹 서비스
 * MetricsHandler와 협력하여 Redis ZSET 기반 랭킹 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final WeightManager weightManager;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 단일 이벤트 랭킹 업데이트 (MetricsHandler에서 호출)
     */
    public void updateRanking(GeneralEnvelopeEvent envelope) {
        String eventType = envelope.type();
        JsonNode payload = envelope.payload();
        
        Double score = calculateScore(eventType, payload);
        Long productId = extractProductId(payload);
        
        if (score != null && productId != null) {
            String key = generateRankingKey(LocalDate.now());
            String member = "product:" + productId;
            
            redisTemplate.opsForZSet().incrementScore(key, member, score);
            setTtlIfNeeded(key);
            
            log.debug("랭킹 업데이트: {} -> {} (+{})", member, key, score);
        }
    }
    
    /**
     * 배치 이벤트 랭킹 업데이트 (MetricsHandler에서 호출)
     */
    public void updateRankingBatch(List<GeneralEnvelopeEvent> envelopes) {
        String todayKey = generateRankingKey(LocalDate.now());
        
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (GeneralEnvelopeEvent envelope : envelopes) {
                Double score = calculateScore(envelope.type(), envelope.payload());
                Long productId = extractProductId(envelope.payload());
                
                if (score != null && productId != null) {
                    String member = "product:" + productId;
                    redisTemplate.opsForZSet().incrementScore(todayKey, member, score);
                }
            }
            return null;
        });
        
        setTtlIfNeeded(todayKey);
        log.info("배치 랭킹 업데이트 완료: {} 개 이벤트", envelopes.size());
    }
    
    private Double calculateScore(String eventType, JsonNode payload) {
        return switch (eventType) {
            case EventTypes.PRODUCT_VIEWED -> weightManager.getWeight(RankingEventType.PRODUCT_VIEWED);
            case EventTypes.PRODUCT_LIKED -> weightManager.getWeight(RankingEventType.PRODUCT_LIKED);
            case EventTypes.PRODUCT_UNLIKED -> -weightManager.getWeight(RankingEventType.PRODUCT_LIKED); // 좋아요 취소
            case EventTypes.ORDER_CREATED -> {
                double orderAmount = extractOrderAmount(payload);
                double weight = weightManager.getWeight(RankingEventType.ORDER_CREATED);
                yield weight * Math.log(1 + orderAmount / 1000.0); // 로그 스케일링
            }
            default -> null;
        };
    }
    
    private Long extractProductId(JsonNode payload) {
        JsonNode productIdNode = payload.get("productId");
        if (productIdNode != null && productIdNode.isNumber()) {
            return productIdNode.asLong();
        }
        
        // OrderCreated 이벤트의 경우 items 배열에서 추출
        JsonNode itemsNode = payload.get("items");
        if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
            JsonNode firstItem = itemsNode.get(0);
            JsonNode itemProductId = firstItem.get("productId");
            if (itemProductId != null && itemProductId.isNumber()) {
                return itemProductId.asLong();
            }
        }
        
        return null;
    }
    
    private double extractOrderAmount(JsonNode payload) {
        JsonNode itemsNode = payload.get("items");
        if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
            JsonNode firstItem = itemsNode.get(0);
            JsonNode priceNode = firstItem.get("price");
            JsonNode quantityNode = firstItem.get("quantity");
            
            if (priceNode != null && quantityNode != null) {
                return priceNode.asDouble() * quantityNode.asInt();
            }
        }
        return 1.0;
    }
    
    private String generateRankingKey(LocalDate date) {
        return "ranking:all:" + date.format(DATE_FORMATTER);
    }
    
    private void setTtlIfNeeded(String key) {
        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null || ttl == -1) {
            redisTemplate.expire(key, Duration.ofDays(2));
        }
    }
}
