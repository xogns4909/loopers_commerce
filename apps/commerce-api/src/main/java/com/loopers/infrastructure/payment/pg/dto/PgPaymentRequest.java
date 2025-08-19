package com.loopers.infrastructure.payment.pg.dto;

import lombok.Builder;

@Builder
public record PgPaymentRequest(
    String orderId,
    String cardType,
    String cardNo,
    String amount,
    String callbackUrl
) {
    public static PgPaymentRequest of(String orderId, long amount, String callbackUrl) {
        return PgPaymentRequest.builder()
            .orderId(orderId)
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9012-3456")
            .amount(String.valueOf(amount))
            .callbackUrl(callbackUrl)
            .build();
    }
}
