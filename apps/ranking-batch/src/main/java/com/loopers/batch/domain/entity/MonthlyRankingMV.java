package com.loopers.batch.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mv_product_rank_monthly",
       uniqueConstraints = @UniqueConstraint(columnNames = {"`year_month`", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyRankingMV extends BaseEntity {
    
    @Column(name = "`year_month`", nullable = false, length = 6)
    private String yearMonth;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "score", nullable = false, precision = 18, scale = 6)
    private BigDecimal score;
    
    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    
    @Builder
    public MonthlyRankingMV(String yearMonth, Long productId, Integer likeCount, Integer orderCount, 
                           Integer viewCount, BigDecimal score, Integer rankNo) {
        this.yearMonth = yearMonth;
        this.productId = productId;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.orderCount = orderCount != null ? orderCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.score = score != null ? score : BigDecimal.ZERO;
        this.rankNo = rankNo;
    }
}
