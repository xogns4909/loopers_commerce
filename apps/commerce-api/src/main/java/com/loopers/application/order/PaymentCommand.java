package com.loopers.application.order;

import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;
import lombok.Builder;
import lombok.Getter;


@Builder
public record PaymentCommand(UserId userId, Long orderId, String cardType, String cardNo, OrderAmount amount, PaymentMethod paymentMethod) {


    
    // 기존 메서드도 유지 (호환성)
    public static PaymentCommand from(OrderCommand command, Order order) {
        return new PaymentCommand(
            command.userId(),
            order.getId(),
            command.CardType(),
            command.CardNo(),
            order.getAmount(),
            command.paymentMethod()
        );
    }

    public static PaymentCommand createByEvent(OrderCreatedEvent event,Order order) {
        return new PaymentCommand(
            order.getUserId(),
            order.getId(),
            event.cardType(),
            event.cardNo(),
            order.getAmount(),
            event.paymentMethod()
        );
    }
}

