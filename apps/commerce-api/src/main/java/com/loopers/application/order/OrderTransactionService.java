package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    private final OrderService orderService;
    private final CompensationService compensationService;

    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderService.getOrder(event.orderId());
        if (order == null || order.getStatus().isFinal()) return;
        if (!order.getStatus().canPayComplete()) return;
        orderService.markPaid(order, event.paymentId());
        log.info("주문 결제 완료 처리됨 - orderId: {}", event.orderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = orderService.getOrder(event.orderId());
        if (order == null || order.getStatus().isFinal()) return;
        if (!order.getStatus().canPayFail()) return;
        orderService.markPaymentFailed(order, event.reason());
        compensationService.reverseFor(order.getId()); // 재고/쿠폰 보상
        log.info("주문 결제 실패 처리됨 - orderId: {}, reason: {}", event.orderId(), event.reason());
    }
}
