package com.loopers.infrastructure.event;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.application.notification.MessageSendRequested;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


@Component
public class EventRoutingStrategy {
    

    private static final Map<EventType, String> TOPIC_MAPPING = Map.of(
        EventType.PAYMENT_COMPLETED, "order-events.v1",
        EventType.PAYMENT_FAILED, "order-events.v1",
        EventType.ORDER_CREATED, "order-events.v1",
        EventType.ORDER_FAILED, "order-events.v1",
        EventType.PRODUCT_LIKED, "catalog-events.v1",
        EventType.PRODUCT_UNLIKED, "catalog-events.v1", 
        EventType.PRODUCT_VIEWED, "catalog-events.v1",
        EventType.MESSAGE_SEND_REQUESTED, "notification-events.v1"
    );
    

    public String getTopic(EventType eventType) {
        return TOPIC_MAPPING.getOrDefault(eventType, "general-events.v1");
    }
    

    public String getKey(Object payload) {
        return switch(payload) {
            case PaymentCompletedEvent e -> e.orderId().toString();
            case PaymentFailedEvent e -> e.orderId().toString();
            case OrderCreatedEvent e -> e.orderId().toString();
            case OrderFailedEvent e -> e.orderId().toString();
            case ProductLikedEvent e -> e.productId().toString();
            case ProductUnlikedEvent e -> e.productId().toString();
            case ProductViewedEvent e -> e.productId().toString();
            case MessageSendRequested e -> e.recipientUserId();
            default -> throw new CoreException(ErrorType.INTERNAL_ERROR,"순서 보장을 위해 도메인 키가 필요합니다: " + payload.getClass().getSimpleName());
        };
    }
}
