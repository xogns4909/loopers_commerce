package com.loopers.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreCalculator {
    
    private final RankingProperties rankingProperties;
    
    public double calculateScore(String eventType) {
        return switch(eventType) {
            case "PRODUCT_VIEWED", "PRODUCT_LIKED", "PRODUCT_UNLIKED", "ORDER_CREATED" -> 1.0;
            default -> 0.0;
        };
    }
    
    public boolean isRankingEvent(String eventType) {
        return calculateScore(eventType) != 0.0;
    }
}
