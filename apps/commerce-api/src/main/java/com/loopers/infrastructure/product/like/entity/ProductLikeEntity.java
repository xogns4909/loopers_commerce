package com.loopers.infrastructure.product.like.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.like.ProductLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLikeEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    public ProductLikeEntity(Long productId, Integer likeCount) {
        this.productId = productId;
        this.likeCount = likeCount;
    }
    
    public static ProductLikeEntity from(ProductLike productLike) {
        ProductLikeEntity entity = new ProductLikeEntity(
            productLike.getProductId(), 
            productLike.getLikeCount()
        );
        return entity;
    }
    
    public ProductLike toModel() {
        return new ProductLike(this.productId, this.likeCount);
    }
    
    public void updateLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
}
