package com.loopers.domain.product.like;

import java.util.Optional;

public interface ProductLikeRepository {
    Optional<ProductLike> findByProductId(Long productId);
    Optional<ProductLike> findByProductIdWithLock(Long productId);
    ProductLike save(ProductLike productLike);


}
