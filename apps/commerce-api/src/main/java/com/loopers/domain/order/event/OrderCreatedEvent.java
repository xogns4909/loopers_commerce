package com.loopers.domain.order.event;

import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;

import java.util.List;

public record OrderCreatedEvent(
    Long orderId,
    UserId userId,
    List<OrderItem> items,
    String cardType,
    String cardNo,
    PaymentMethod paymentMethod
) {
    public static OrderCreatedEvent of(Long orderId, UserId userId, List<OrderItem> items, 
                                     String cardType, String cardNo, PaymentMethod paymentMethod) {
        return new OrderCreatedEvent(orderId, userId, items, cardType, cardNo, paymentMethod);
    }
}
