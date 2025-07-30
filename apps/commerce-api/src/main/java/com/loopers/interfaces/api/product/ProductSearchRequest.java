package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductSortType;

public record ProductSearchRequest(
    int page,
    int size,
    ProductSortType sortBy,
    Long brandId
) {
    public int offset() {
        return page * size;
    }
}
