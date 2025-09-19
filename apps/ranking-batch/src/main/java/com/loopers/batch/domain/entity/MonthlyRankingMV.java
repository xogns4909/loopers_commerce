package com.loopers.batch.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
    name = "mv_product_rank_monthly",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_monthly_product_target", columnNames = {"product_id", "target_date"})
    },
    indexes = {
        @Index(name = "idx_monthly_target_score", columnList = "target_date, ranking_score DESC"),
        @Index(name = "idx_monthly_product_target", columnList = "product_id, target_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyRankingMV extends BaseEntity {

    // 테이블엔 있을 수 있으나 앱에서 세팅 안 함
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

    // 주간에서 삽질했던 reflection 제거: 그냥 필드로 둔다
    @Column(name = "rank_no")
    private Integer rankNo;

    @Builder
    public MonthlyRankingMV(
        Long productId,
        LocalDate targetDate,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        Long viewCount,
        Long likeCount,
        Long orderCount,
        BigDecimal rankingScore,
        Integer rankNo
    ) {
        this.productId = productId;
        this.targetDate = targetDate;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.orderCount = orderCount;
        this.rankingScore = rankingScore;
        this.rankNo = rankNo;
    }

    public void setRankNo(Integer rankNo) {
        this.rankNo = rankNo;
    }
}
