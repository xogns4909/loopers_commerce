package com.loopers.application.order;

import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OrderFacade 테스트")
class OrderFacadeTest {

    private OrderService orderService;
    private PaymentService paymentService;
    private OrderProcessor orderProcessor;
    private OrderRequestHistoryService orderRequestHistoryService;

    private OrderFacade orderFacade;

    private static final String CARD_TYPE = "SAMSUNG";
    private static final String CARD_NO = "1234-1234-1234-1234";

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        paymentService = mock(PaymentService.class);
        orderProcessor = mock(OrderProcessor.class);
        orderRequestHistoryService = mock(OrderRequestHistoryService.class);
        orderFacade = new OrderFacade(paymentService, orderService, orderRequestHistoryService, orderProcessor);
    }

    @Test
    @DisplayName("주문 및 결제 성공")
    void order_success() {
        // given
        UserId userId = UserId.of("kth4909");
        Price itemPrice = Price.of(new BigDecimal("500"));
        var itemsCmd = List.of(new OrderCommand.OrderItemCommand(1L, 2, itemPrice, "idem-123", 1L));
        var cmd = new OrderCommand(userId, itemsCmd, com.loopers.domain.payment.model.PaymentMethod.POINT,
            CARD_TYPE, CARD_NO, itemPrice, "idem-123", 1L);


        var orderItems = List.of(new OrderItem(1L, 2, itemPrice));
        var amount = OrderAmount.of(new BigDecimal("500"));
        Order order = Order.reconstruct(
            1L, userId, orderItems, OrderStatus.PENDING, amount, null
        );

        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("idem-123"))
            .thenReturn(Optional.empty());
        when(orderProcessor.process(cmd)).thenReturn(order);

        // when
        OrderResponse response = orderFacade.order(cmd);

        // then
        verify(orderProcessor).process(cmd);
        verify(paymentService).pay(any()); // 결제 트리거 확인

        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualTo(amount.value());
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("멱등키가 이미 존재하면 기존 주문을 반환한다")
    void order_whenIdempotentKeyExists_returnExistingOrder() {
        // given
        UserId userId = UserId.of("kth4909");
        Long existingOrderId = 99L;

        var existingItems = List.of(new OrderItem(1L, 2, Price.of(new BigDecimal("20000"))));
        var existingAmount = OrderAmount.of(new BigDecimal("20000"));
        Order existingOrder = Order.reconstruct(
            existingOrderId, userId, existingItems, OrderStatus.COMPLETED, existingAmount, null
        );

        when(orderRequestHistoryService.findOrderIdByIdempotencyKey("idemp-001"))
            .thenReturn(Optional.of(existingOrderId));
        when(orderService.getOrder(existingOrderId)).thenReturn(existingOrder);

        var cmd = new OrderCommand(
            userId,
            List.of(new OrderCommand.OrderItemCommand(1L, 2, Price.of(new BigDecimal("20000")), "idemp-001", 1L)),
            com.loopers.domain.payment.model.PaymentMethod.POINT,
            CARD_TYPE, CARD_NO,
            Price.of(new BigDecimal("20000")),
            "idemp-001",
            null
        );

        // when
        OrderResponse response = orderFacade.order(cmd);


        verify(orderService, never()).createOrder(any(), any(), any(), any());
        verify(paymentService, never()).pay(any());
        verify(orderService, never()).completeOrder(any());

        assertThat(response.orderId()).isEqualTo(existingOrderId);
        assertThat(response.amount()).isEqualTo(existingAmount.value());
        assertThat(response.status()).isEqualTo(OrderStatus.COMPLETED);
    }
}
