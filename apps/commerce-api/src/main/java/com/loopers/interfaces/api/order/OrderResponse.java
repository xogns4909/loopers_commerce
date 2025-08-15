package com.loopers.interfaces.api.order;

import com.loopers.domain.order.model.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(Long orderId, BigDecimal amount, OrderStatus status) {
}
