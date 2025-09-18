package com.loopers.batch.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * 주간 상품 랭킹 집계 테이블
 */
@Entity
@Table(name = "mv_product_rank_weekly",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_target", columnNames = {"product_id", "target_date"})
    },
    indexes = {
        @Index(name = "idx_target_score", columnList = "target_date, ranking_score DESC"),
        @Index(name = "idx_product_target", columnList = "product_id, target_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRankingMV extends BaseEntity {

    @Column(name = "published_at", insertable = false, updatable = false)
    private ZonedDateTime publishedAt;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;

    @Column(name = "ranking_score", nullable = false, precision = 10, scale = 4)
    private BigDecimal rankingScore = BigDecimal.ZERO;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    // 순위 세팅용(리플렉션 말고 메서드로)
    public void assignRank(int rank) {
        this.rankNo = rank;
    }

    @Builder
    public WeeklyRankingMV(Long productId, LocalDate targetDate, LocalDate periodStartDate,
        LocalDate periodEndDate, Long viewCount, Long likeCount,
        Long orderCount, BigDecimal rankingScore) {
        this.productId = productId;
        this.targetDate = targetDate;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.orderCount = orderCount;
        this.rankingScore = rankingScore;
    }
}
