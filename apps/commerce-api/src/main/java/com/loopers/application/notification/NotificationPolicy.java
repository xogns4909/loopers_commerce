package com.loopers.application.notification;

import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.infrastructure.event.DomainEventBridge;
import com.loopers.infrastructure.event.Envelope;
import com.loopers.infrastructure.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationPolicy {

    private final DomainEventBridge eventBridge;

    @Async("notificationsExec")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(Envelope<OrderCreatedEvent> envelope) {
        if (!EventType.ORDER_CREATED.getValue().equals(envelope.type())) return;

        OrderCreatedEvent event = envelope.payload();
        String templateId = "ORDER_CREATED_ko";
        String locale = "ko-KR";

        // 브릿지를 통해 알림 이벤트 발행
        eventBridge.publish(new MessageSendRequested(
            MessageSendRequested.Channel.KAKAO, templateId, event.userId().value(), locale,
            Map.of(
                "orderId", String.valueOf(event.orderId()),
                "itemCount", String.valueOf(event.items().size()),
                "originalMessageId", envelope.messageId() // 추적 정보 추가
            )
        ));
    }
}
