package com.loopers.application.order;

import static org.mockito.Mockito.verify;

import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventHandler 단위 테스트")
class OrderEventHandlerTest {

    @Mock
    private OrderTransactionService orderTransactionService;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("PaymentCompletedEvent 수신 시 주문 완료 처리")
    void payment_completed_event_handles_order_completion() {
        // given
        PaymentCompletedEvent event = PaymentCompletedEvent.of(
            1L, 100L, UserId.of("testUser"), "tx_123456"
        );

        // when
        orderEventHandler.onPaymentCompleted(event);

        // then
        verify(orderTransactionService).handlePaymentCompleted(event);
    }

    @Test
    @DisplayName("PaymentFailedEvent 수신 시 주문 실패 처리")
    void payment_failed_event_handles_order_failure() {
        // given
        PaymentFailedEvent event = PaymentFailedEvent.of(
            1L, 100L, UserId.of("testUser"), "PG 서버 오류", "tx_123456"
        );

        // when
        orderEventHandler.onPaymentFailed(event);

        // then
        verify(orderTransactionService).handlePaymentFailed(event);
    }
}
