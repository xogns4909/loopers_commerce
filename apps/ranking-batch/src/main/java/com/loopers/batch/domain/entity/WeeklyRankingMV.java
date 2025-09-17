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
@Table(name = "mv_product_rank_weekly",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year_week", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRankingMV extends BaseEntity {
    
    @Column(name = "year_week", nullable = false, length = 8)
    private String yearWeek;
    
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
    
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;
    
    @Builder
    public WeeklyRankingMV(String yearWeek, Long productId, Integer likeCount, Integer orderCount, 
                          Integer viewCount, BigDecimal score, Integer rankNo, LocalDateTime publishedAt) {
        this.yearWeek = yearWeek;
        this.productId = productId;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.orderCount = orderCount != null ? orderCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.score = score != null ? score : BigDecimal.ZERO;
        this.rankNo = rankNo;
        this.publishedAt = publishedAt != null ? publishedAt : LocalDateTime.now();
    }
}
