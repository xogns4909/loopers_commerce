package com.loopers.interfaces.api.like;


public record LikedProductResponse(
    Long productId,
    String productName,
    int price,
    boolean liked
) { }

