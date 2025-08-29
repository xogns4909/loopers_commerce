package com.loopers.infrastructure.payment.pg.dto;

public record PgPaymentResponse(
    String transactionKey,
    String status,
    String message
) {
}
