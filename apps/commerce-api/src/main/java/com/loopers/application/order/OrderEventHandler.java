package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderService orderService;

    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        Order order = orderService.getOrder(event.orderId());
        if (order != null && !order.getStatus().isFinal()) {
            orderService.completeOrder(order);
        }
    }

    @EventListener
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {

        Order order = orderService.getOrder(event.orderId());
        if (order != null && !order.getStatus().isFinal()) {
            Order failedOrder = order.fail();
            orderService.save(failedOrder);
        }
    }

}
