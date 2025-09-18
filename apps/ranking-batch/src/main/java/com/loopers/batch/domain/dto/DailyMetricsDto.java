package com.loopers.batch.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * event_metrics에서 일별로 집계한 데이터 전송 객체
 */
public record DailyMetricsDto(
    Long productId,
    LocalDate statDate,
    Long likeCount,
    Long orderCount, 
    Long viewCount
) {
    
    public DailyMetricsDto {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID는 양수여야 합니다");
        }
        if (statDate == null) {
            throw new IllegalArgumentException("통계 날짜는 필수입니다");
        }
        if (likeCount == null || likeCount < 0) likeCount = 0L;
        if (orderCount == null || orderCount < 0) orderCount = 0L;
        if (viewCount == null || viewCount < 0) viewCount = 0L;
    }
    
    /**
     * 활동이 있는 상품인지 확인
     */
    public boolean hasActivity() {
        return likeCount + orderCount + viewCount > 0;
    }
    
    /**
     * 총 활동 수
     */
    public long getTotalActivity() {
        return likeCount + orderCount + viewCount;
    }
    
    /**
     * 가중치를 적용한 점수 계산
     */
    public BigDecimal calculateScore(double likeWeight, double orderWeight, double viewWeight) {
        BigDecimal score = BigDecimal.valueOf(likeCount * likeWeight)
            .add(BigDecimal.valueOf(orderCount * orderWeight))
            .add(BigDecimal.valueOf(viewCount * viewWeight));
        
        return score.max(BigDecimal.ZERO);
    }
}
