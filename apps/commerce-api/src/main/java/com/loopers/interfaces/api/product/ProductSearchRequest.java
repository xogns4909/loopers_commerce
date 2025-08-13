package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductSortType;
import org.springframework.web.bind.annotation.RequestParam;

public record ProductSearchRequest(
    @RequestParam(defaultValue = "0")
    int page,
    @RequestParam(defaultValue = "20")
    int size,
    @RequestParam(required = false)
    ProductSortType sortBy,
    @RequestParam(required = false)
    Long brandId
) {
    public ProductSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }
}
