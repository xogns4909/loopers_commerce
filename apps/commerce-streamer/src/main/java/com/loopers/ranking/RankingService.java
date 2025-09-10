package com.loopers.ranking;

import com.loopers.event.GeneralEnvelopeEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String signalKey = keyGenerator.generateSignalKey(LocalDate.now(), event.type());
        
        // 신호별 ZSET에만 저장 (점수 1.0 고정)
        redisTemplate.opsForZSet().incrementScore(signalKey, member, 1.0);
        redisTemplate.expire(signalKey, Duration.ofDays(30));
        
        log.debug("Ranking signal updated - key: {}, productId: {}", signalKey, productId);
    }
    
    public void updateRankingBatch(List<GeneralEnvelopeEvent> events) {
        if (!rankingProperties.isEnabled() || events.isEmpty()) {
            return;
        }
        
        Map<String, Map<Long, Long>> eventCounts = events.stream()
            .filter(event -> scoreCalculator.isRankingEvent(event.type()))
            .filter(event -> extractProductId(event.payload()) != null)
            .collect(Collectors.groupingBy(
                GeneralEnvelopeEvent::type,
                Collectors.groupingBy(
                    event -> extractProductId(event.payload()),
                    Collectors.counting()
                )
            ));
        
        if (eventCounts.isEmpty()) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Map.Entry<String, Map<Long, Long>> eventEntry : eventCounts.entrySet()) {
                String eventType = eventEntry.getKey();
                String signalKey = keyGenerator.generateSignalKey(today, eventType);
                
                for (Map.Entry<Long, Long> productEntry : eventEntry.getValue().entrySet()) {
                    Long productId = productEntry.getKey();
                    Long count = productEntry.getValue();
                    String member = keyGenerator.generateProductMember(productId);
                    
                    connection.zIncrBy(signalKey.getBytes(), count.doubleValue(), member.getBytes());
                }
                
                int ttlSeconds = 30 * 24 * 3600; // 30일
                connection.expire(signalKey.getBytes(), ttlSeconds);
            }
            return null;
        });
        
        log.info("Ranking batch updated - events: {}, unique products: {}", 
                events.size(), 
                eventCounts.values().stream().mapToLong(m -> m.size()).sum());
    }
    
    private Long extractProductId(JsonNode payload) {
        if (payload == null || !payload.has("productId")) {
            log.warn("payload에 productId가 없습니다: {}", payload);
            return null;
        }
        return payload.get("productId").asLong();
    }
}
