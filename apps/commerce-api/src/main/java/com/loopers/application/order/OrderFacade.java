package com.loopers.application.order;


import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final OrderRequestHistoryService orderRequestHistoryService;

    @Transactional
    public OrderResponse order(OrderCommand command) {
        productService.checkAndDeduct(command.items());

        Order order = orderService.createOrder(command.userId(), command.items());
        orderRequestHistoryService.savePending(command.idempotencyKey(), command.userId().value(), order.getId());

        paymentService.pay(new PaymentCommand(
            command.userId(),
            order.getId(),
            order.getAmount(),
            command.paymentMethod()
        ));

        orderService.completeOrder(order);
        orderRequestHistoryService.markSuccess(command.idempotencyKey());

        return new OrderResponse(order.getId(), order.getAmount().value(), order.getStatus());
    }

    public Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command) {
        return orderService.getUserOrders(command);
    }

    public OrderDetailResponse getOrderDetail(OrderDetailCommand command) {
        return orderService.getOrderDetail(command);
    }
}


