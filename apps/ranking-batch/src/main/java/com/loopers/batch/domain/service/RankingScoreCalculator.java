package com.loopers.batch.domain.service;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
public class RankingScoreCalculator {
    
    private final double likeWeight;
    private final double orderWeight;
    private final double viewWeight;
    
    public RankingScoreCalculator(
        @Value("${ranking.weights.like:0.2}") double likeWeight,
        @Value("${ranking.weights.order:0.7}") double orderWeight,
        @Value("${ranking.weights.view:0.1}") double viewWeight
    ) {
        validateWeights(likeWeight, orderWeight, viewWeight);
        this.likeWeight = likeWeight;
        this.orderWeight = orderWeight;
        this.viewWeight = viewWeight;
        
        log.info("랭킹 점수 계산기 초기화 - Like: {}, Order: {}, View: {}", 
                likeWeight, orderWeight, viewWeight);
    }
    

    public BigDecimal calculateScore(DailyMetricsDto metrics) {
        if (!metrics.hasActivity()) {
            return BigDecimal.ZERO;
        }
        
        return metrics.calculateScore(likeWeight, orderWeight, viewWeight);
    }
    

    private void validateWeights(double like, double order, double view) {
        if (like < 0 || order < 0 || view < 0) {
            throw new IllegalArgumentException("가중치는 음수일 수 없습니다");
        }
        
        if (like + order + view == 0) {
            throw new IllegalArgumentException("모든 가중치가 0일 수 없습니다");
        }
    }
    
    public double getLikeWeight() { return likeWeight; }
    public double getOrderWeight() { return orderWeight; }
    public double getViewWeight() { return viewWeight; }
}
