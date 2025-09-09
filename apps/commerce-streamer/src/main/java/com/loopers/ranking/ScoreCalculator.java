package com.loopers.ranking;

import static com.loopers.event.EventTypes.*;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreCalculator {
    
    private final RankingProperties rankingProperties;
    

    public double calculateScore(String eventType) {
        var weights = rankingProperties.getScore().getWeights();
        
        return switch(eventType) {
            case PRODUCT_VIEWED -> weights.getView();
            case PRODUCT_LIKED -> weights.getLike();
            case PRODUCT_UNLIKED -> weights.getUnlike();
            case ORDER_CREATED -> weights.getOrder();
            default -> 0.0;
        };
    }
    

    public boolean isRankingEvent(String eventType) {
        return calculateScore(eventType) != 0.0;
    }
}
