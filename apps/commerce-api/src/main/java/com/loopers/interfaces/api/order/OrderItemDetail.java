package com.loopers.interfaces.api.order;

public record OrderItemDetail(
    Long productId,
    String productName,
    int quantity,
    Long price
) {}
