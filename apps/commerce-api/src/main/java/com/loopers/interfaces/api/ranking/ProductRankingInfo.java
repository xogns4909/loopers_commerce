package com.loopers.interfaces.api.ranking;

import java.time.LocalDate;

/**
 * 개별 상품의 랭킹 정보
 */
public record ProductRankingInfo(
    Long productId,
    LocalDate date,
    Long rank,      // 순위 (1-based, null이면 랭킹 없음)
    Double score    // 점수 (null이면 점수 없음)
) {
    
    public static ProductRankingInfo of(Long productId, LocalDate date, Long rank, Double score) {
        return new ProductRankingInfo(productId, date, rank, score);
    }
    
    public boolean isInRanking() {
        return rank != null && score != null;
    }
}
