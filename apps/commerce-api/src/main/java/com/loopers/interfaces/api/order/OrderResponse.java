package com.loopers.interfaces.api.order;

import java.math.BigDecimal;

public record OrderResponse(Long orderId, BigDecimal amount, com.loopers.domain.order.model.OrderStatus status) {
}
