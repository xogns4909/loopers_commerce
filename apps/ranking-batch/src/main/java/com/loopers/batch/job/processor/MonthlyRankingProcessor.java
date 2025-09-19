package com.loopers.batch.job.processor;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import com.loopers.batch.domain.dto.MonthlyMetricsDto;
import com.loopers.batch.domain.entity.MonthlyRankingMV;
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
public class MonthlyRankingProcessor implements ItemProcessor<MonthlyMetricsDto, MonthlyRankingMV> {

    private final RankingScoreCalculator scoreCalculator;

    @Override
    public MonthlyRankingMV process(MonthlyMetricsDto item) {
        BigDecimal score = scoreCalculator.calculateScore(
            new DailyMetricsDto(
                item.getProductId(),
                item.getTargetDate(),
                item.getLikeCount(),
                item.getOrderCount(),
                item.getViewCount()
            )
        );

        log.debug("월간 랭킹 처리 - 상품ID: {}, 대상날짜: {}, 점수: {}",
            item.getProductId(), item.getTargetDate(), score);

        return MonthlyRankingMV.builder()
            .productId(item.getProductId())
            .targetDate(item.getTargetDate())
            .periodStartDate(item.getPeriodStartDate())
            .periodEndDate(item.getPeriodEndDate())
            .viewCount(item.getViewCount())
            .likeCount(item.getLikeCount())
            .orderCount(item.getOrderCount())
            .rankingScore(score)
            .build();
    }
}
