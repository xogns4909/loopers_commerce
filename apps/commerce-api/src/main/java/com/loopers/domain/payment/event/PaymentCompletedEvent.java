package com.loopers.domain.payment.event;

import com.loopers.domain.user.model.UserId;

public record PaymentCompletedEvent(
    Long paymentId,
    Long orderId, 
    UserId userId,
    String transactionKey
) {
    public static PaymentCompletedEvent of(Long paymentId, Long orderId, UserId userId, String transactionKey) {
        return new PaymentCompletedEvent(paymentId, orderId, userId, transactionKey);
    }
}
