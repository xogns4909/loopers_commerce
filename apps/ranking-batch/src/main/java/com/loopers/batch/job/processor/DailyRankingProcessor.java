package com.loopers.batch.job.processor;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import com.loopers.batch.domain.entity.DailyRankingMV;
import com.loopers.batch.domain.service.RankingScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * DailyMetricsDto를 DailyRankingMV 엔티티로 변환하는 Processor
 * 
 * 책임:
 * - 메트릭 데이터를 엔티티로 변환
 * - 점수 계산 로직 적용 (위임)
 * - 비즈니스 규칙 적용 (활동 없는 상품 필터링)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyRankingProcessor implements ItemProcessor<DailyMetricsDto, DailyRankingMV> {
    
    private final RankingScoreCalculator scoreCalculator;
    
    @Override
    public DailyRankingMV process(DailyMetricsDto metrics) {
        // 활동이 없는 상품 필터링
        if (!metrics.hasActivity()) {
            log.debug("상품 {} - 활동 없음, 건너뜀", metrics.productId());
            return null;
        }
        
        // 점수 계산 (책임 위임)
        BigDecimal score = scoreCalculator.calculateScore(metrics);
        
        // 엔티티 생성
        DailyRankingMV entity = DailyRankingMV.builder()
            .productId(metrics.productId())
            .statDate(metrics.statDate())
            .likeCount(metrics.likeCount().intValue())
            .orderCount(metrics.orderCount().intValue())
            .viewCount(metrics.viewCount().intValue())
            .score(score)
            .build();
        
        log.debug("상품 {} 처리 완료 - 점수: {}, 활동: {}", 
                 metrics.productId(), score, metrics.getTotalActivity());
        
        return entity;
    }
}
