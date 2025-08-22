package com.loopers.interfaces.api.payment;

import java.time.LocalDateTime;

public record PaymentCallbackRequest(
    String transactionKey,
    String orderId,
    String status,
    String amount,
    String reason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
