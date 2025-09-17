package com.loopers.batch.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mv_product_rank_daily",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "stat_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRankingMV extends BaseEntity {
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "score", nullable = false, precision = 18, scale = 6)
    private BigDecimal score;
    
    @Builder
    public DailyRankingMV(Long productId, LocalDate statDate, 
                         Integer likeCount, Integer orderCount, Integer viewCount, BigDecimal score) {
        this.productId = productId;
        this.statDate = statDate;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.orderCount = orderCount != null ? orderCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.score = score != null ? score : BigDecimal.ZERO;
    }
}
