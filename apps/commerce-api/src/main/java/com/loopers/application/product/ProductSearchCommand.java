package com.loopers.application.product;

import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.product.ProductSearchRequest;
import org.springframework.data.domain.Pageable;

public record ProductSearchCommand(
    Pageable pageable,
    ProductSortType sortType,
    Long brandId
) {
    public static ProductSearchCommand from(ProductSearchRequest request, Pageable pageable) {
        return new ProductSearchCommand(
            pageable,
            request.sortBy(),
            request.brandId()
        );
    }
}
