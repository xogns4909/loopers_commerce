package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Like(Long id, String userId, Long productId) {

    public static Like of(Long id, String userId, Long productId) {
        if (userId == null || productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        return new Like(id, userId, productId);
    }
}
