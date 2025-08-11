package com.loopers.application.order;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.order.OrderResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderProcessor {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final OrderRequestHistoryService orderRequestHistoryService;

    public Order process(OrderCommand command) {

        productService.checkAndDeduct(command.items());

        List<OrderItem> items = command.toItems();

        BigDecimal originalAmount = OrderAmount.from(items).value();

        BigDecimal finalAmount = couponService.apply(command.userId(),command.couponId(), originalAmount);

        Order order = orderService.createOrder(command.userId(), command.items(), OrderAmount.of(finalAmount));
        orderRequestHistoryService.savePending(command.idempotencyKey(),command.userId().value(),order.getId());

        return order;
    }

    public OrderResponse completeOrder(Order completeOrder,String idempotencyKey){
        orderService.completeOrder(completeOrder);
        orderRequestHistoryService.markSuccess(idempotencyKey);

        return new OrderResponse(completeOrder.getId(), completeOrder.getAmount().value(), completeOrder.getStatus());
    }
}
