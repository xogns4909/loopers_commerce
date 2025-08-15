package com.loopers.domain.product.like;

public interface ProductLikeService {
    void incrementLike(Long productId);
    void decrementLike(Long productId);
    int getLikeCount(Long productId);
}
