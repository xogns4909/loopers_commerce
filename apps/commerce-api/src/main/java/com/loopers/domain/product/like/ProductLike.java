package com.loopers.domain.product.like;

import lombok.Getter;

@Getter
public class ProductLike {
    private final Long productId;
    private final Integer likeCount;
    
    public ProductLike(Long productId, Integer likeCount) {
        this.productId = productId;
        this.likeCount = likeCount;
    }
    
    public static ProductLike create(Long productId) {
        return new ProductLike(productId, 0);
    }

    public static ProductLike of(Long productId, int i) {
        return new ProductLike(productId, i);
    }


    public ProductLike incrementLike() {
        return new ProductLike(this.productId, this.likeCount + 1);
    }
    
    public ProductLike decrementLike() {
        return new ProductLike(this.productId, Math.max(0, this.likeCount - 1));
    }
}
