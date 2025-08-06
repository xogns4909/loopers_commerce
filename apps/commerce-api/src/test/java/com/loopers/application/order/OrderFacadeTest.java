package com.loopers.application.order;


import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderFacadeTest {

    private OrderService orderService = mock(OrderService.class);
    private PaymentService paymentService = mock(PaymentService.class);
    private ProductService productService = mock(ProductService.class);
    private CouponService couponService = mock(CouponService.class);
    private OrderRequestHistoryService orderRequestHistoryService = mock(OrderRequestHistoryService.class);
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderFacade = new OrderFacade(orderService, paymentService, productService, couponService, orderRequestHistoryService);
    }

    @Test
    @DisplayName("주문 및 결제 성공")
    void order_success_Test() {
        // given
        UserId userId = UserId.of("kth4909");
        Price itemPrice = Price.of(BigDecimal.valueOf(500));
        PaymentMethod method = PaymentMethod.POINT;

        List<OrderCommand.OrderItemCommand> items = List.of(
            new OrderCommand.OrderItemCommand(1L, 2, itemPrice, "123",1L)
        );

        OrderCommand command = new OrderCommand(userId, items, method, itemPrice, "123", 1L);

        List<OrderItem> orderItems = List.of(new OrderItem(1L, 2, itemPrice));
        Order order = Order.create(userId, orderItems, null);

        // stubbing
        when(orderService.createOrder(userId, items, null)).thenReturn(order);
        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("123")).thenReturn(Optional.empty());

        // when
        OrderResponse response = orderFacade.order(command);

        // then
        verify(productService).checkAndDeduct(items);
        verify(orderService).createOrder(userId, items, null);
        verify(paymentService).pay(any());
        verify(orderService).completeOrder(order);
        verify(orderRequestHistoryService).savePending("123", userId.value(), order.getId());
        verify(orderRequestHistoryService).markSuccess("123");

        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.amount()).isEqualTo(order.getAmount().value());
        assertThat(response.status()).isEqualTo(order.getStatus());
    }

    @Test
    @DisplayName("멱등키가 이미 존재하면 기존 주문을 반환한다")
    void order_whenIdempotentKeyExists_returnExistingOrder() {
        // given
        UserId userId = UserId.of("kth4909");
        Long existingOrderId = 99L;
        Order existingOrder = Order.create(userId, List.of(
            new OrderItem(1L, 2, Price.of(BigDecimal.valueOf(10000)))
        ), null);

        ReflectionTestUtils.setField(existingOrder, "id", existingOrderId);
        existingOrder.complete();

        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("idemp-001")).thenReturn(Optional.of(existingOrderId));
        when(orderService.getOrder(existingOrderId)).thenReturn(existingOrder);

        // when
        OrderCommand command = new OrderCommand(
            userId,
            List.of(new OrderCommand.OrderItemCommand(1L, 2, Price.of(BigDecimal.valueOf(10000)), "idemp-001",1L)),
            PaymentMethod.POINT,
            Price.of(BigDecimal.valueOf(20000)),
            "idemp-001",
            null
        );

        OrderResponse response = orderFacade.order(command);

        // then
        assertThat(response.orderId()).isEqualTo(existingOrderId);
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(response.status().name()).isEqualTo("COMPLETED");

        verify(orderService, never()).createOrder(any(), any(), any());
        verify(paymentService, never()).pay(any());
        verify(orderService, never()).completeOrder(any());
    }
}
