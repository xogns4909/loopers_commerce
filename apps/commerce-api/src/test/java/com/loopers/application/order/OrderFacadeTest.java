package com.loopers.application.order;


import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
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
    private OrderProcessor orderProcessor = mock(OrderProcessor.class);
    private OrderRequestHistoryService orderRequestHistoryService = mock(OrderRequestHistoryService.class);
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderFacade = new OrderFacade(paymentService,orderService,orderRequestHistoryService,orderProcessor);
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
        OrderAmount orderAmount = OrderAmount.of(BigDecimal.valueOf(500));
        List<OrderItem> orderItems = List.of(new OrderItem(1L, 2, itemPrice));
        Order order = Order.create(userId, orderItems, orderAmount);
        ReflectionTestUtils.setField(order, "id", 1L);
        OrderResponse fakeResponse = new OrderResponse(order.getId(), order.getAmount().value(), OrderStatus.PENDING);

        // stubbing
        when(orderService.createOrder(userId, orderItems, null)).thenReturn(order);
        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("123")).thenReturn(Optional.empty());
        when(orderProcessor.process(command)).thenReturn(order);
//        when(orderProcessor.completeOrder(order, command.idempotencyKey())).thenReturn(fakeResponse);


        // when
        OrderResponse response = orderFacade.order(command);
        when(orderProcessor.process(command)).thenReturn(order);

        // then
        verify(orderProcessor).process(command);
        verify(paymentService).pay(any());

        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualTo(order.getAmount().value());
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }


    @Test
    @DisplayName("멱등키가 이미 존재하면 기존 주문을 반환한다")
    void order_whenIdempotentKeyExists_returnExistingOrder() {
        // given
        UserId userId = UserId.of("kth4909");
        Long existingOrderId = 99L;
        Order existingOrder = Order.create(userId, List.of(
            new OrderItem(1L, 2, Price.of(BigDecimal.valueOf(10000)))
        ), OrderAmount.of(BigDecimal.valueOf(20000)));

        ReflectionTestUtils.setField(existingOrder, "id", existingOrderId);
        existingOrder.complete();

        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("idemp-001")).thenReturn(Optional.of(existingOrderId));
        when(orderService.getOrder(existingOrderId)).thenReturn(existingOrder);

        // when
        OrderCommand command = new OrderCommand(
            userId,
            List.of(new OrderCommand.OrderItemCommand(1L, 2, Price.of(BigDecimal.valueOf(20000)), "idemp-001",1L)),
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
