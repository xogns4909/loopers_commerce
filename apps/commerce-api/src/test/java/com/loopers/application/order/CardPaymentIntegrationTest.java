package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.PaymentDataService;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.strategy.CardPaymentStrategy;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("CARD 결제 통합 테스트")
class CardPaymentIntegrationTest {

    @Mock
    private PgPaymentGateway pgGateway;
    
    @Mock
    private PaymentDataService paymentDataService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CardPaymentStrategy cardPaymentStrategy;

    private PaymentCommand paymentCommand;
    private final Long PAYMENT_ID = 1L;
    private final Long ORDER_ID = 100L;
    private final String USER_ID = "testUser";

    @BeforeEach
    void setUp() {
        paymentCommand = new PaymentCommand(
            UserId.of(USER_ID),
            ORDER_ID,
            OrderAmount.of(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD
        );
    }

    @Test
    @DisplayName("카드 결제 전체 성공 시나리오")
    void card_payment_full_success_scenario() {
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123456", "SUCCESS", "결제 요청 성공");
        when(pgGateway.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenReturn(pgResponse);

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then
        verify(paymentDataService).createInitiatedPayment(paymentCommand);
        verify(paymentDataService).updateToProcessing(PAYMENT_ID, "tx_123456");
        verify(pgGateway).requestPayment(eq(USER_ID), any(PgPaymentRequest.class));
    }

    @Test
    @DisplayName("PG 서버 500 에러 시 실패 이벤트 발행")
    void pg_server_500_error_publishes_failed_event() {
        // given:
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        when(pgGateway.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출 실패(CB/Retry 이후): 서버 오류"));

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then:
        verify(paymentDataService).createInitiatedPayment(paymentCommand);
        verify(paymentDataService).updateToFailed(PAYMENT_ID, "PG 요청 무효");
        verify(eventPublisher).publishEvent(any(com.loopers.domain.payment.event.PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("PG 타임아웃 시 실패 이벤트 발행")
    void pg_timeout_publishes_failed_event() {
        // given
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        when(pgGateway.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출 실패(CB/Retry 이후): 타임아웃"));

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then
        verify(paymentDataService).updateToFailed(PAYMENT_ID, "PG 요청 무효");
        verify(eventPublisher).publishEvent(any(com.loopers.domain.payment.event.PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("여러 결제 요청 동시 처리")
    void multiple_payment_requests_handling() {
        // given
        PaymentCommand command1 = createPaymentCommand("user1", 101L);
        PaymentCommand command2 = createPaymentCommand("user2", 102L);
        PaymentCommand command3 = createPaymentCommand("user3", 103L);

        when(paymentDataService.createInitiatedPayment(any())).thenReturn(1L, 2L, 3L);
        
        PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123", "SUCCESS", "성공");
        when(pgGateway.requestPayment(any(), any())).thenReturn(pgResponse);

        // when
        cardPaymentStrategy.pay(command1);
        cardPaymentStrategy.pay(command2);
        cardPaymentStrategy.pay(command3);

        // then
        verify(paymentDataService, org.mockito.Mockito.times(3)).createInitiatedPayment(any());
        verify(pgGateway, org.mockito.Mockito.times(3)).requestPayment(any(), any());
    }

    private PaymentCommand createPaymentCommand(String userId, Long orderId) {
        return new PaymentCommand(
            UserId.of(userId),
            orderId,
            OrderAmount.of(BigDecimal.valueOf(5000)),
            PaymentMethod.CARD
        );
    }
}
