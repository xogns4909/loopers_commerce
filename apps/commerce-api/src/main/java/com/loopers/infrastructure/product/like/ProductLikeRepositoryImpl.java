package com.loopers.infrastructure.product.like;

import com.loopers.domain.product.like.ProductLike;
import com.loopers.domain.product.like.ProductLikeRepository;
import com.loopers.infrastructure.product.like.entity.ProductLikeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final JPAProductLikeRepository jpaRepository;

    @Override
    public Optional<ProductLike> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId)
            .map(ProductLikeEntity::toModel);
    }

    @Override
    public Optional<ProductLike> findByProductIdWithLock(Long productId) {
        return jpaRepository.findByProductIdForUpdate(productId)
            .map(ProductLikeEntity::toModel);
    }

    @Override
    public ProductLike save(ProductLike productLike) {

        ProductLikeEntity entity = jpaRepository.findByProductId(productLike.getProductId())
            .orElseGet(() -> ProductLikeEntity.from(productLike));


        entity.updateLikeCount(productLike.getLikeCount());

        ProductLikeEntity saved = jpaRepository.save(entity);
        return saved.toModel();
    }


}
