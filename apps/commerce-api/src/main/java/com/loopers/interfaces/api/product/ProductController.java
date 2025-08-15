package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacade productFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getProducts(
        @RequestParam(defaultValue = "0")
        int page,
        @RequestParam(defaultValue = "20")
        int size,
        @RequestParam(required = false)
        ProductSortType sortBy,
        @RequestParam(required = false)
        Long brandId,
        Pageable pageable
    ) {
        // 안전한 값으로 request 생성
        ProductSearchRequest request = new ProductSearchRequest(
            Math.max(0, page),
            Math.min(Math.max(1, size), 100),
            sortBy,
            brandId
        );
        
        Page<ProductInfo> pageResult = productFacade.getProducts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(ProductListResponse.from(pageResult)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
        @PathVariable Long productId
    ) {
        ProductResponse product = productFacade.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
