package com.loopers.domain.like;

import com.loopers.application.like.LikeCommand;
import com.loopers.domain.like.LikeResult;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.like.LikedProductResponse;

import java.util.List;

public interface LikeService {

    LikeResult like(LikeCommand command);

    LikeResult unlike(LikeCommand command);

    List<LikedProductResponse> getLikedProductInfos(UserId id);
}
