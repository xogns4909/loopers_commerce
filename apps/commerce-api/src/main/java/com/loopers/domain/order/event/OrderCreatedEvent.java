package com.loopers.domain.order.event;

import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.user.model.UserId;

import java.util.List;

public record OrderCreatedEvent(
    Long orderId,
    UserId userId,
    List<OrderItem> items
) {
    public static OrderCreatedEvent of(Long orderId, UserId userId, List<OrderItem> items) {
        return new OrderCreatedEvent(orderId, userId, items);
    }
}
