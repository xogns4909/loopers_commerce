package com.loopers.interfaces.api.product;


import com.loopers.domain.common.Money;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.product.model.Product;
import com.loopers.domain.product.model.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
    Long productId,
    String productName,
    String description,
    Price price,
    ProductStatus status,
    int stockQuantity,
    String brandName,
    int likeCount
){}
