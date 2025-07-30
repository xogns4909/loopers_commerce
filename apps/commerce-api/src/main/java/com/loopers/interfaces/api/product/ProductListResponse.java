package com.loopers.interfaces.api.product;


import com.loopers.application.product.ProductInfo;
import java.util.List;
import org.springframework.data.domain.Page;

public record ProductListResponse(
    List<ProductInfo> content,
    int page,
    int size,
    int totalPages,
    long totalElements
) {
    public static ProductListResponse from(Page<ProductInfo> page) {
        return new ProductListResponse(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }
}
