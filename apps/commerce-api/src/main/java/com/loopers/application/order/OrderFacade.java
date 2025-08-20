package com.loopers.application.order;

import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.PaymentService;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderRequestHistoryService orderRequestHistoryService;
    private final OrderProcessor orderProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public OrderResponse order(OrderCommand command) {
        log.info("주문 처리 시작 - userId: {}, idempotencyKey: {}", command.userId(), command.idempotencyKey());

        return findExistingOrderResponse(command)
            .orElseGet(() -> createAndPayOrder(command));
    }


    private Optional<OrderResponse> findExistingOrderResponse(OrderCommand command) {
        return orderRequestHistoryService.findOrderIdByIdempotencyKey(command.idempotencyKey())
            .map(orderService::getOrder)
            .map(order -> new OrderResponse(order.getId(), order.getAmount().value(), order.getStatus()));
    }

    private OrderResponse createAndPayOrder(OrderCommand command) {
        try {

            Order order = createOrderInTransaction(command);
            log.info("주문 생성 완료 - orderId: {}", order.getId());

            paymentService.pay(PaymentCommand.from(command, order));
            log.info("결제 요청 완료 - orderId: {}", order.getId());

            // 3. 주문 요청 기록 완료 (개별 트랜잭션)
            return completeOrderRequestInTransaction(order, command.idempotencyKey());

        } catch (Exception e) {
            log.error("주문 처리 실패 - userId: {}, error: {}", command.userId(), e.getMessage());


            eventPublisher.publishEvent(OrderFailedEvent.of(
                null, command.userId(), e.getMessage()
            ));

            throw e;
        }
    }

    private Order createOrderInTransaction(OrderCommand command) {
        return orderProcessor.process(command);
    }


    private OrderResponse completeOrderRequestInTransaction(Order order, String idempotencyKey) {
        orderRequestHistoryService.markSuccess(idempotencyKey);
        return new OrderResponse(order.getId(), order.getAmount().value(), order.getStatus());
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command) {
        return orderService.getUserOrders(command);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(OrderDetailCommand command) {
        return orderService.getOrderDetail(command);
    }
}
