package com.loopers.domain.payment.event;

import com.loopers.domain.user.model.UserId;

public record PaymentFailedEvent(
    Long paymentId,
    Long orderId,
    UserId userId,
    String reason,
    String transactionKey
) {
    public static PaymentFailedEvent of(Long paymentId, Long orderId, UserId userId, String reason, String transactionKey) {
        return new PaymentFailedEvent(paymentId, orderId, userId, reason, transactionKey);
    }
}
