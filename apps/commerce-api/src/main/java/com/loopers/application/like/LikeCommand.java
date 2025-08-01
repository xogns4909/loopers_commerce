package com.loopers.application.like;

import com.loopers.domain.user.model.UserId;


public record LikeCommand(UserId userId, Long productId) {}
