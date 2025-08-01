package com.loopers.domain.like;


import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.like.LikedProductResponse;
import java.util.List;

public interface LikeRepository {

    boolean exists(UserId userId, Long productId);

    void save(Like like);

    void delete(UserId userId, Long productId);

    List<LikedProductResponse> findLikedProductsWithInfo(UserId userId);
}
