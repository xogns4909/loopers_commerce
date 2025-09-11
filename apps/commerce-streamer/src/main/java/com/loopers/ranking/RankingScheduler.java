package com.loopers.ranking;

import static com.loopers.event.EventTypes.ORDER_CREATED;
import static com.loopers.event.EventTypes.PRODUCT_LIKED;
import static com.loopers.event.EventTypes.PRODUCT_VIEWED;

import com.loopers.ranking.RankingProperties.Score.Weights;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RankingKeyGenerator keyGenerator;
    private final RankingProperties rankingProperties;
    
    @Scheduled(cron = "30 0 12 * * *", zone = "Asia/Seoul")
    public void carryOverDailyRanking() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        log.info("Starting daily ranking carry-over: {} -> {}", yesterday, today);
        

        createDailySummary(yesterday);
        

        applyCarryOver(yesterday, today);
        
        log.info("Daily ranking carry-over completed: {} -> {}", yesterday, today);
    }
    
    private void createDailySummary(LocalDate date) {
        String sumKey = keyGenerator.generateSumKey(date);
        
        if (redisTemplate.hasKey(sumKey)) {
            log.info("Daily summary already exists: {}", sumKey);
            return;
        }
        
        String viewKey = keyGenerator.generateSignalKey(date, PRODUCT_VIEWED);
        String likeKey = keyGenerator.generateSignalKey(date, PRODUCT_LIKED);
        String orderKey = keyGenerator.generateSignalKey(date, ORDER_CREATED);

        Weights weights = rankingProperties.getScore().getWeights();

        var viewProducts = redisTemplate.opsForZSet().rangeWithScores(viewKey, 0, -1);
        var likeProducts = redisTemplate.opsForZSet().rangeWithScores(likeKey, 0, -1);
        var orderProducts = redisTemplate.opsForZSet().rangeWithScores(orderKey, 0, -1);
        

        if (viewProducts != null) {
            for (var tuple : viewProducts) {
                redisTemplate.opsForZSet().add(sumKey, tuple.getValue(), tuple.getScore() * weights.getView());
            }
        }
        if (likeProducts != null) {
            for (var tuple : likeProducts) {
                redisTemplate.opsForZSet().incrementScore(sumKey, tuple.getValue(), tuple.getScore() * weights.getLike());
            }
        }
        if (orderProducts != null) {
            for (var tuple : orderProducts) {
                redisTemplate.opsForZSet().incrementScore(sumKey, tuple.getValue(), tuple.getScore() * weights.getOrder());
            }
        }
        
        redisTemplate.expire(sumKey, Duration.ofDays(2));
        log.info("Daily summary created: {}", sumKey);
    }
    
    private void applyCarryOver(LocalDate yesterday, LocalDate today) {
        String yesterdaySum = keyGenerator.generateSumKey(yesterday);
        String todaySum = keyGenerator.generateSumKey(today);
        



        var top20 = redisTemplate.opsForZSet().reverseRangeWithScores(yesterdaySum, 0, 19);
        
        if (top20 != null && !top20.isEmpty()) {
            double carryWeight = 0.1;
            

            for (var tuple : top20) {
                String member = tuple.getValue();
                Double score = tuple.getScore();
                if (member != null && score != null) {
                    redisTemplate.opsForZSet().add(todaySum, member, score * carryWeight);
                }
            }
            
            redisTemplate.expire(todaySum, Duration.ofDays(1));
            log.info("Carry-over applied: {} products to {}", top20.size(), todaySum);
        }
    }
}
