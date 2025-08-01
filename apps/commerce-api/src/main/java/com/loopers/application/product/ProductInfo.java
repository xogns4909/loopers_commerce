package com.loopers.application.product;

import com.querydsl.core.annotations.QueryProjection;

public record ProductInfo(
    Long productId,
    String productName,
    String brandName,
    int price,
    int likeCount
) {
    @QueryProjection
    public ProductInfo {
    }
}
