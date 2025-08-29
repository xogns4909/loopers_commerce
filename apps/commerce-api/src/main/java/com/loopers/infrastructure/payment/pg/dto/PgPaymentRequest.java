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
    public static PgPaymentRequest of(String orderId,String cardType, String cardNo, long amount, String callbackUrl) {
        return PgPaymentRequest.builder()
            .orderId(orderId)
            .cardType(cardType)
            .cardNo(cardNo)
            .amount(String.valueOf(amount))
            .callbackUrl(callbackUrl)
            .build();
    }
}
