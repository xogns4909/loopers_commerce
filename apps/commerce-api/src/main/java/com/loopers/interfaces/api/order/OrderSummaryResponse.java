package com.loopers.interfaces.api.order;

import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

public record OrderSummaryResponse(Long orderId, BigDecimal amount, String status, LocalDateTime createdAt) {

    @QueryProjection
    public OrderSummaryResponse {
    }
}
