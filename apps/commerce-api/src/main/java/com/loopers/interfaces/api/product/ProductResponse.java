package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public record ProductResponse(
    Long id,
    String name,
    String brandName,
    int price,
    int likeCount
) {

    public static ProductResponse from(ProductInfo info) {
        return new ProductResponse(
            info.productId(),
            info.productName(),
            info.brandName(),
            info.price(),
            info.likeCount()
        );
    }
}
