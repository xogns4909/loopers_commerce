package com.loopers.application.order;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderProcessor {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final OrderRequestHistoryService orderRequestHistoryService;

    @Transactional
    public Order process(OrderCommand command) {
        // 재고 차감 (동시성 고려: 비관/낙관락은 ProductService 내부에서 처리)
        productService.checkAndDeduct(command.items());

        // 실제 상품 가격으로 OrderItem 생성
        List<OrderItem> items = createOrderItemsWithRealPrice(command.items());
        BigDecimal originalAmount = OrderAmount.from(items).value();
        BigDecimal finalAmount = couponService.apply(command.userId(), command.couponId(), originalAmount);


        Order order = orderService.createOrder(command.userId(), items, OrderAmount.of(finalAmount),command.couponId());

        // 요청 히스토리: RECEIVED (이름만 바꿨고 의미는 '요청 접수')
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
