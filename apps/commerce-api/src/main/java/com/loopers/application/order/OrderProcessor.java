package com.loopers.application.order;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderProcessor {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final OrderRequestHistoryService orderRequestHistoryService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public Order process(OrderCommand command) {

        productService.checkAndDeduct(command.items());


        List<OrderItem> items = createOrderItemsWithRealPrice(command.items());
        BigDecimal originalAmount = OrderAmount.from(items).value();
        BigDecimal finalAmount = couponService.apply(command.userId(), command.couponId(), originalAmount);


        Order order = orderService.createOrder(command.userId(), items, OrderAmount.of(finalAmount),command.couponId());

        publisher.publishEvent(OrderCreatedEvent.of(order.getId(), command.userId(), items));

        orderRequestHistoryService.saveReceived(command.idempotencyKey(), command.userId().value(), order.getId());
        return order;
    }

    private List<OrderItem> createOrderItemsWithRealPrice(List<OrderCommand.OrderItemCommand> itemCommands) {
        return itemCommands.stream()
                .map(this::createOrderItemWithRealPrice)
                .toList();
    }

    private OrderItem createOrderItemWithRealPrice(OrderCommand.OrderItemCommand itemCommand) {
        // 실제 상품 정보 조회해서 가격 설정
        ProductInfo product = productService.getProduct(itemCommand.productId());
        return new OrderItem(
                itemCommand.productId(), 
                itemCommand.quantity(), 
                Price.of(new BigDecimal(product.price()))
        );
    }
}
