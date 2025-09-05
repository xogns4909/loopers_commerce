package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.infrastructure.event.Envelope;
import com.loopers.infrastructure.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderTransactionService orderTransactionService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(Envelope<PaymentCompletedEvent> envelope) {
        if (!EventType.PAYMENT_COMPLETED.getValue().equals(envelope.type())) return;

        log.info("PaymentCompleted 이벤트 처리 - messageId: {}, correlationId: {}, occurredAt: {}",
            envelope.messageId(), envelope.correlationId(), envelope.occurredAt());

        orderTransactionService.handlePaymentCompleted(envelope.payload());
    }



    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onPaymentFailed(Envelope<PaymentFailedEvent> envelope) {
        if (!EventType.PAYMENT_FAILED.getValue().equals(envelope.type())) return;

        log.info("PaymentFailed 이벤트 처리 - messageId: {}, orderId: {}, paymentId: {}",
            envelope.messageId(), envelope.payload().orderId(), envelope.payload().paymentId());

        orderTransactionService.handlePaymentFailed(envelope.payload());
    }
}

