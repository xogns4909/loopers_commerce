package com.loopers.infrastructure.product.like;

import com.loopers.infrastructure.product.like.entity.ProductLikeEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JPAProductLikeRepository extends JpaRepository<ProductLikeEntity, Long> {

    Optional<ProductLikeEntity> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductLikeEntity p where p.productId = :productId")
    Optional<ProductLikeEntity> findByProductIdForUpdate(@Param("productId") Long productId);

}
