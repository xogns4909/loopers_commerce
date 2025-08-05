package com.loopers.application.order;

import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderFacadeTest {

    private OrderService orderService = mock(OrderService.class);
    private PaymentService paymentService = mock(PaymentService.class);
    private ProductService productService = mock(ProductService.class);
    private OrderRequestHistoryService orderRequestHistoryService = mock(OrderRequestHistoryService.class);
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderFacade = new OrderFacade(orderService, paymentService, productService, orderRequestHistoryService);
    }

    @Test
    @DisplayName("주문 및 결제 성공")
    void order_success_Test() {
        // given
        UserId userId = UserId.of("kth4909");
        Price itemPrice = Price.of(BigDecimal.valueOf(500));
        PaymentMethod method = PaymentMethod.POINT;

        List<OrderCommand.OrderItemCommand> items = List.of(
            new OrderCommand.OrderItemCommand(1L, 2, itemPrice, "123")
        );

        OrderCommand command = new OrderCommand(userId, items, method, itemPrice, "123");

        // 실제 도메인 객체 생성
        List<OrderItem> orderItems = List.of(new OrderItem(1L, 2, itemPrice));
        Order order = Order.create(userId, orderItems);

        // stubbing
        when(orderService.createOrder(userId, items)).thenReturn(order);

        // when
        OrderResponse response = orderFacade.order(command);

        // then
        verify(productService).checkAndDeduct(items);
        verify(orderService).createOrder(userId, items);
        verify(paymentService).pay(any());
        verify(orderService).completeOrder(order);
        verify(orderRequestHistoryService).savePending("123", userId.value(), order.getId());
        verify(orderRequestHistoryService).markSuccess("123");

        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.amount()).isEqualTo(order.getAmount().value());
        assertThat(response.status()).isEqualTo(order.getStatus());
    }
}
