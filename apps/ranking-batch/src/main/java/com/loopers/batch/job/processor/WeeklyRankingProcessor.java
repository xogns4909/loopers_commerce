package com.loopers.batch.job.processor;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import com.loopers.batch.domain.dto.WeeklyMetricsDto;
import com.loopers.batch.domain.entity.WeeklyRankingMV;
import com.loopers.batch.domain.service.RankingScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeeklyRankingProcessor implements ItemProcessor<WeeklyMetricsDto, WeeklyRankingMV> {
    
    private final RankingScoreCalculator scoreCalculator;
    
    @Override
    public WeeklyRankingMV process(WeeklyMetricsDto item) throws Exception {
        
        // 주간 랭킹 점수 계산 (개별 메트릭 값으로 계산)
        BigDecimal weeklyScore = scoreCalculator.calculateScore(
            new DailyMetricsDto(
                item.getProductId(),
                item.getTargetDate(), 
                item.getLikeCount(),
                item.getOrderCount(),
                item.getViewCount()
            )
        );
        
        log.debug("주간 랭킹 처리 - 상품ID: {}, 대상날짜: {}, 점수: {}", 
                 item.getProductId(), item.getTargetDate(), weeklyScore);
        
        return WeeklyRankingMV.builder()
                .productId(item.getProductId())
                .targetDate(item.getTargetDate())
                .periodStartDate(item.getPeriodStartDate())
                .periodEndDate(item.getPeriodEndDate())
                .viewCount(item.getViewCount())
                .likeCount(item.getLikeCount())
                .orderCount(item.getOrderCount())
                .rankingScore(weeklyScore)
                .build();
    }
}
