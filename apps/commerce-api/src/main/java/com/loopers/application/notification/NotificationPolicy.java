package com.loopers.application.notification;

import com.loopers.domain.order.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Map;

@Component
public class NotificationPolicy {

    private final ApplicationEventPublisher publisher;

    public NotificationPolicy(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Async("notificationsExec")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {

        String templateId = "ORDER_CREATED_ko";
        String locale = "ko-KR";

        publisher.publishEvent(new MessageSendRequested(
            MessageSendRequested.Channel.KAKAO, templateId, e.userId().value(), locale,
            Map.of(
                "orderId", String.valueOf(e.orderId()),
                "itemCount", String.valueOf(e.items().size())
            )
        ));
    }
}
