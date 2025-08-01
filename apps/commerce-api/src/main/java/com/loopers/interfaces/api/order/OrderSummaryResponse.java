package com.loopers.interfaces.api.order;

import com.loopers.domain.order.model.OrderStatus;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;


public record OrderSummaryResponse(Long orderId, Long amount, OrderStatus status, ZonedDateTime createdAt) {

    @QueryProjection
    public OrderSummaryResponse {
    }
}
