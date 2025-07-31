package com.loopers.interfaces.api.like;

public record LikeResponse(Long productId, boolean liked) {

    public static LikeResponse liked(Long productId) {
        return new LikeResponse(productId, true);
    }

    public static LikeResponse unliked(Long productId) {
        return new LikeResponse(productId, false);
    }

    public static LikeResponse alreadyLiked(Long productId) {
        return new LikeResponse(productId, true);
    }

    public static LikeResponse notLiked(Long productId) {
        return new LikeResponse(productId, false);
    }
}
