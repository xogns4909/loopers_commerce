package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
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
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        orderTransactionService.handlePaymentCompleted(event);
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("PaymentFailedEvent 처리 시작 - orderId: {}, paymentId: {}", event.orderId(), event.paymentId());
        orderTransactionService.handlePaymentFailed(event);
    }
}

