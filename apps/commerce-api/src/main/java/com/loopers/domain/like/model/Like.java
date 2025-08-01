package com.loopers.domain.like.model;

import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Like(Long id, UserId userId, Long productId) {

    public static Like create(UserId userId, Long productId) {
        if (userId == null || productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        return new Like(null, userId, productId);
    }

    public static Like reconstruct(Long id, UserId userId, Long productId) {
        if (userId == null || productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        return new Like(id, userId, productId);
    }
}
