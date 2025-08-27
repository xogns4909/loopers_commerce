package com.loopers.application.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRequestHandler 단위 테스트")
class PaymentRequestHandlerTest {

    @Mock
    private PaymentService paymentService;
    
    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentRequestHandler paymentRequestHandler;

    @Test
    @DisplayName("OrderCreatedEvent 수신 시 결제 요청")
    void order_created_event_triggers_payment() {
        // given
        UserId userId = UserId.of("testUser");
        Long orderId = 1L;
        
        List<OrderItem> items = List.of(
            new OrderItem(1L, 2, Price.of(BigDecimal.valueOf(5000)))
        );
        
        OrderCreatedEvent event = OrderCreatedEvent.of(
            orderId, 
            userId, 
            items,
            "SAMSUNG",
            "1234-1234-1234-1234",
            PaymentMethod.CARD
        );

        Order mockOrder = createMockOrder(orderId, userId, items);
        given(orderService.getOrder(orderId)).willReturn(mockOrder);

        // when
        paymentRequestHandler.handleOrderCreated(event);

        // then
        verify(orderService).getOrder(orderId);
        verify(paymentService).pay(any(PaymentCommand.class));
    }

    private Order createMockOrder(Long orderId, UserId userId, List<OrderItem> items) {
        return Order.reconstruct(
            orderId,
            userId,
            items,
            OrderStatus.PENDING,
            OrderAmount.of(BigDecimal.valueOf(10000)),
            null // usedCouponId
        );
    }
}
