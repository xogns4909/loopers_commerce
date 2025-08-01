package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductSortType;

public record ProductSearchRequest(
    int page,
    int size,
    ProductSortType sortBy,
    Long brandId
) {
}
