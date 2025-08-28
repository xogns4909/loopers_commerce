package com.loopers.application.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.model.*;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.event.DomainEventBridge;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderProcessorEventTest {

    @Test
    void process_publishes_OrderCreatedEvent() {
        // mocks
        ProductService productService = mock(ProductService.class);
        CouponService couponService = mock(CouponService.class);
        OrderService orderService = mock(OrderService.class);
        OrderRequestHistoryService history = mock(OrderRequestHistoryService.class);
        DomainEventBridge eventBridge = mock(DomainEventBridge.class);

        // SUT
        OrderProcessor sut = new OrderProcessor(productService, couponService, orderService, history, eventBridge);

        // given
        UserId userId = UserId.of("u1");
        OrderItemCommand itemCmd = new OrderItemCommand(1L, 2, Price.of(new BigDecimal("1000")), "idem-1", 1L);
        OrderCommand cmd = new OrderCommand(userId, List.of(itemCmd),

            PaymentMethod.CARD, "S", "1111",
            Price.of(new BigDecimal("2000")), "idem-1", 1L);

        // 실제 가격 조회 모킹
        when(productService.getProduct(1L)).thenReturn(new ProductInfo(1L, "Product","brand", 1000,3));
        // 쿠폰 적용 모킹
        when(couponService.apply(eq(userId), any(), any())).thenReturn(new BigDecimal("2000"));
        // 주문 생성 모킹
        List<OrderItem> items = List.of(new OrderItem(1L, 2, Price.of(new BigDecimal("1000"))));
        Order order = Order.reconstruct(10L, userId, items, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("2000")), null);
        when(orderService.createOrder(eq(userId), any(), any(), any())).thenReturn(order);

        // when
        sut.process(cmd);

        // then:
        ArgumentCaptor<OrderCreatedEvent> cap = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventBridge, times(1)).publish(cap.capture());
        OrderCreatedEvent evt = cap.getValue();

        Assertions.assertThat(evt.orderId()).isEqualTo(10L);
        Assertions.assertThat(evt.userId()).isEqualTo(userId);
        Assertions.assertThat(evt.items()).hasSize(1);
    }
}
