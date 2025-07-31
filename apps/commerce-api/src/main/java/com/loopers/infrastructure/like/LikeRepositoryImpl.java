package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.like.entity.LikeEntity;
import com.loopers.infrastructure.like.entity.QLikeEntity;
import com.loopers.infrastructure.product.entity.QProductEntity;
import com.loopers.interfaces.api.like.LikedProductResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final JPAlikeRepository jpAlikeRepository;
    private final JPAQueryFactory queryFactory;

    private static final QLikeEntity like = QLikeEntity.likeEntity;
    private static final QProductEntity product = QProductEntity.productEntity;


    @Override
    public boolean exists(UserId userId, Long productId) {
        return jpAlikeRepository.existsByUserIdAndProductId(userId.value(), productId);
    }


    public void save(Like like) {
        jpAlikeRepository.save(LikeEntity.from(like));
    }

    @Override
    public void delete(UserId userId, Long productId) {
        jpAlikeRepository.deleteByUserIdAndProductId(userId.value(), productId);
    }

    @Override
    public List<LikedProductResponse> findLikedProductsWithInfo(UserId userId) {

        return queryFactory
            .select(Projections.constructor(LikedProductResponse.class,
                product.id,
                product.name,
                product.price,
                Expressions.constant(true)
            ))
            .from(like)
            .join(product).on(like.productId.eq(product.id))
            .where(like.userId.eq(userId.value()))
            .fetch();
    }
}
