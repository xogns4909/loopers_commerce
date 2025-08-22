package com.loopers.domain.order.event;

import com.loopers.domain.user.model.UserId;

public record OrderFailedEvent(
    Long orderId,
    UserId userId,
    String reason
) {
    public static OrderFailedEvent of(Long orderId, UserId userId, String reason) {
        return new OrderFailedEvent(orderId, userId, reason);
    }
}
