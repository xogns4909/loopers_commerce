package com.loopers.infrastructure.event;

import com.loopers.application.notification.MessageSendRequested;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventBridge {

    private final ApplicationEventPublisher publisher;
    private final EnvelopeFactory envelopeFactory;

    // Payment Events
    public void publish(PaymentCompletedEvent event) {
        Envelope<PaymentCompletedEvent> envelope =
            envelopeFactory.create(EventType.PAYMENT_COMPLETED, event);

        log.debug("Publishing PaymentCompleted event - messageId: {}, paymentId: {}",
            envelope.messageId(), event.paymentId());

        publisher.publishEvent(envelope);
    }

    public void publish(PaymentFailedEvent event) {
        Envelope<PaymentFailedEvent> envelope =
            envelopeFactory.create(EventType.PAYMENT_FAILED, event);

        log.debug("Publishing PaymentFailed event - messageId: {}, paymentId: {}",
            envelope.messageId(), event.paymentId());

        publisher.publishEvent(envelope);
    }

    // Order Events
    public void publish(OrderCreatedEvent event) {
        Envelope<OrderCreatedEvent> envelope =
            envelopeFactory.create(EventType.ORDER_CREATED, event);

        log.debug("Publishing OrderCreated event - messageId: {}, orderId: {}",
            envelope.messageId(), event.orderId());

        publisher.publishEvent(envelope);
    }

    public void publish(OrderFailedEvent event) {
        Envelope<OrderFailedEvent> envelope =
            envelopeFactory.create(EventType.ORDER_FAILED, event);

        log.debug("Publishing OrderFailed event - messageId: {}, orderId: {}",
            envelope.messageId(), event.orderId());

        publisher.publishEvent(envelope);
    }

    // Like Events
    public void publish(ProductLikedEvent event) {
        Envelope<ProductLikedEvent> envelope =
            envelopeFactory.create(EventType.PRODUCT_LIKED, event);

        log.debug("Publishing ProductLiked event - messageId: {}, productId: {}",
            envelope.messageId(), event.productId());

        publisher.publishEvent(envelope);
    }

    public void publish(ProductUnlikedEvent event) {
        Envelope<ProductUnlikedEvent> envelope =
            envelopeFactory.create(EventType.PRODUCT_UNLIKED, event);

        log.debug("Publishing ProductUnliked event - messageId: {}, productId: {}",
            envelope.messageId(), event.productId());

        publisher.publishEvent(envelope);
    }


    public void publish(MessageSendRequested event) {
        Envelope<MessageSendRequested> envelope =
            envelopeFactory.create(EventType.MESSAGE_SEND_REQUESTED, event);

        log.debug("Publishing MessageSendRequested event - messageId: {}, channel: {}",
            envelope.messageId(), event.channel());

        publisher.publishEvent(envelope);
    }
}
