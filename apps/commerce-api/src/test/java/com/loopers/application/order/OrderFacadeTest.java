package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
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
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderFacade = new OrderFacade(orderService, paymentService, productService);
    }

    @Test
    @DisplayName("주문 및 결제 성공 -")
    void order_success_Test() {
        // given
        UserId userId = UserId.of("kth4909");
        Long orderId = 1L;
        Price itemPrice = Price.of(BigDecimal.valueOf(500));
        OrderAmount amount = new OrderAmount(BigDecimal.valueOf(1000));
        PaymentMethod method = PaymentMethod.POINT;

        List<OrderCommand.OrderItemCommand> items = List.of(
            new OrderCommand.OrderItemCommand(1L, 2, itemPrice)
        );

        OrderCommand command = new OrderCommand(userId, items, method, itemPrice);
        Order order = mock(Order.class);

        when(orderService.createOrder(userId, items)).thenReturn(order);
        when(order.getId()).thenReturn(orderId);
        when(order.getAmount()).thenReturn(amount);
        when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);

        // when
        OrderResponse response = orderFacade.order(command);

        // then
        verify(productService).checkAndDeduct(items);
        verify(orderService).createOrder(userId, items);
        verify(paymentService).pay(any());
        verify(order).complete();
        verify(orderService).save(order);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.amount()).isEqualTo(amount.value());
        assertThat(response.status()).isEqualTo(OrderStatus.COMPLETED);
    }
}
