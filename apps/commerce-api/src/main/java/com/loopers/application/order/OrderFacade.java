package com.loopers.application.order;


import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.PaymentService;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {


    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderRequestHistoryService orderRequestHistoryService;
    private final OrderProcessor orderProcessor;

    @Transactional
    public OrderResponse order(OrderCommand command) {
        return findExistingOrderResponse(command)
            .orElseGet(() -> createAndPayOrder(command));
    }

    private Optional<OrderResponse> findExistingOrderResponse(OrderCommand command) {
        return orderRequestHistoryService.findOrderIdByIdempotencyKey(command.idempotencyKey())
            .map(orderService::getOrder)
            .map(order -> new OrderResponse(order.getId(), order.getAmount().value(), order.getStatus()));
    }

    private OrderResponse createAndPayOrder(OrderCommand command) {
        Order order = orderProcessor.process(command);
        paymentService.pay(PaymentCommand.from(command, order));
        return orderProcessor.completeOrder(order, command.idempotencyKey());
    }


    public Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command) {
        return orderService.getUserOrders(command);
    }

    public OrderDetailResponse getOrderDetail(OrderDetailCommand command) {
        return orderService.getOrderDetail(command);
    }
}


