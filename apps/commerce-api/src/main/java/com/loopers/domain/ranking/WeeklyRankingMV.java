// WeeklyRankingMV.java
package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Immutable // MV 스냅샷이면 읽기 전용 권장
@Table(name = "mv_product_rank_weekly",
    indexes = {
        @Index(name = "idx_w_target_rank", columnList = "target_date, rank_no"),
        @Index(name = "idx_w_product_target", columnList = "product_id, target_date")
    })
public class WeeklyRankingMV extends BaseEntity {

    // BaseEntity에 id가 있다면 여기서 id 선언하지 마!

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
