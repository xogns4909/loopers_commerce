package com.loopers.application.product.like;

import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.support.annotation.HandleConcurrency;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeServiceImpl implements ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    @HandleConcurrency
    @Transactional
    public void incrementLike(Long productId) {
        ProductLike like = productLikeRepository.findByProductIdWithLock(productId)
            .orElse(ProductLike.create(productId));

        ProductLike incrementLike = like.incrementLike();
        productLikeRepository.save(incrementLike);
    }

    @HandleConcurrency
    @Transactional
    public void decrementLike(Long productId) {
        ProductLike like = productLikeRepository.findByProductIdWithLock(productId)
            .orElse(ProductLike.create(productId));

        ProductLike decrementLike = like.decrementLike();
        productLikeRepository.save(decrementLike);
    }

    @Transactional
    public int getLikeCount(Long productId) {
        return productLikeRepository.findByProductId(productId)
            .map(ProductLike::getLikeCount)
            .orElse(0);
    }
}
