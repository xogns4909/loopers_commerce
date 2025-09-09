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

        log.debug("RankingService.updateRanking({})", event.toString());

        if (!rankingProperties.isEnabled()) {
            return;
        }
        
        if (!scoreCalculator.isRankingEvent(event.type())) {
            return;
        }
        
        String key = keyGenerator.generateDailyKey(LocalDate.now());
        Long productId = extractProductId(event.payload());
        double score = scoreCalculator.calculateScore(event.type());
        
        if (productId != null) {
            String member = keyGenerator.generateProductMember(productId);
            

            redisTemplate.opsForZSet().incrementScore(key, member, score);
            redisTemplate.expire(key, Duration.ofHours(rankingProperties.getTtl().getHours()));
            
            log.debug("Ranking updated - key: {}, prodFinished callinguctId: {}, score: {}", key, productId, score);
        }
    }
    
    private Long extractProductId(JsonNode payload) {
        if (payload == null || !payload.has("productId")) {
            log.warn("payload에 productId가 없습니다: {}", payload);
            return null;
        }
        return payload.get("productId").asLong();
    }
}
