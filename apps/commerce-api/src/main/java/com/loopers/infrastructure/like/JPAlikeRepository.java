package com.loopers.infrastructure.like;

import com.loopers.infrastructure.like.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAlikeRepository extends JpaRepository<LikeEntity,Long> {

    boolean existsByUserIdAndProductId(String value, Long productId);

    void deleteByUserIdAndProductId(String value, Long productId);
}
