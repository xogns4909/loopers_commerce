// MonthlyRankingMV.java
package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Immutable
@Table(name = "mv_product_rank_monthly",
    indexes = {
        @Index(name = "idx_m_target_rank", columnList = "target_date, rank_no"),
        @Index(name = "idx_m_product_target", columnList = "product_id, target_date")
    })
public class MonthlyRankingMV extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "ranking_score", nullable = false)
    private Double rankingScore;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "order_count")
    private Long orderCount;

    @Column(name = "view_count")
    private Long viewCount;
}
