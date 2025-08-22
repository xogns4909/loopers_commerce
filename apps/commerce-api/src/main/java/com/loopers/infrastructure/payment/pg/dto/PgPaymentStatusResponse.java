package com.loopers.infrastructure.payment.pg.dto;

import java.time.LocalDateTime;

public record PgPaymentStatusResponse(
    String transactionKey,
    String orderId,
    String status,
    String amount,
    String reason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
