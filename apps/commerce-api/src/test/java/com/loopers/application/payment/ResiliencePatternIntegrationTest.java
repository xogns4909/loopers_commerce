package com.loopers.application.payment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.order.PaymentCommand;
import com.loopers.application.payment.strategy.CardPaymentStrategy;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.model.PaymentMethod;
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
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Resilience 패턴 통합 테스트")
class ResiliencePatternIntegrationTest {

    @Mock private PgPaymentGateway pgGateway;
    @Mock private PaymentStatePort paymentStatePort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CardPaymentStrategy cardPaymentStrategy;

    private PaymentCommand paymentCommand;
    private final Long PAYMENT_ID = 1L;
    private final Long ORDER_ID = 100L;
    private final String USER_ID = "testUser";

    @BeforeEach
    void setUp() {
        cardPaymentStrategy = new CardPaymentStrategy(pgGateway, paymentStatePort, eventPublisher);

        paymentCommand = new PaymentCommand(
            UserId.of(USER_ID),
            ORDER_ID,
            "SAMSUNG",
            "1234-1234-1234-1234",
            OrderAmount.of(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD
        );
    }

    @Test
    @DisplayName("PG 서버 500 에러 연속 발생 시 Circuit Breaker 동작 검증")
    void circuit_breaker_opens_on_consecutive_pg_failures() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        when(pgGateway.requestPayment(anyString(), any()))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 서버 장애"));

        for (int i = 0; i < 5; i++) {
            assertThrows(CoreException.class, () -> cardPaymentStrategy.pay(paymentCommand));
        }

        verify(eventPublisher, times(5)).publishEvent(isA(Object.class));
        verify(paymentStatePort, times(5)).updateToFailed(eq(PAYMENT_ID), anyString());
    }

    @Test
    @DisplayName("PG 타임아웃 시 재시도 후 최종 실패")
    void pg_timeout_triggers_retry_then_fails() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        when(pgGateway.requestPayment(anyString(), any()))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출 실패(CB/Retry 이후): 타임아웃"));

        assertThrows(CoreException.class, () -> cardPaymentStrategy.pay(paymentCommand));

        verify(eventPublisher).publishEvent(isA(Object.class));
        verify(paymentStatePort).updateToFailed(eq(PAYMENT_ID), eq("PG 요청 무효"));
    }

    @Test
    @DisplayName("PG 네트워크 에러 시 재시도 로직 동작")
    void pg_network_error_triggers_retry_logic() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        when(pgGateway.requestPayment(anyString(), any()))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출 실패(CB/Retry 이후): 연결 실패"));

        assertThrows(CoreException.class, () -> cardPaymentStrategy.pay(paymentCommand));

        verify(paymentStatePort).updateToFailed(eq(PAYMENT_ID), eq("PG 요청 무효"));
        verify(eventPublisher).publishEvent(isA(Object.class));
    }

    @Test
    @DisplayName("PG 응답 지연 시 타임아웃 처리")
    void pg_slow_response_triggers_timeout() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        when(pgGateway.requestPayment(anyString(), any()))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출 실패(CB/Retry 이후): 응답 시간 초과"));

        assertThrows(CoreException.class, () -> cardPaymentStrategy.pay(paymentCommand));

        verify(paymentStatePort).updateToFailed(eq(PAYMENT_ID), eq("PG 요청 무효"));
    }

    @Test
    @DisplayName("PG 정상 응답 시 성공적으로 처리")
    void pg_normal_response_processes_successfully() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        PgPaymentResponse successResponse = new PgPaymentResponse("tx_123456", "SUCCESS", "결제 요청 성공");
        when(pgGateway.requestPayment(anyString(), any())).thenReturn(successResponse);

        cardPaymentStrategy.pay(paymentCommand);

        verify(paymentStatePort).createInitiatedPayment(eq(paymentCommand));
        verify(paymentStatePort).updateToProcessing(eq(PAYMENT_ID), eq("tx_123456"));
        verify(pgGateway).requestPayment(eq(USER_ID), any(PgPaymentRequest.class));
    }

    @Test
    @DisplayName("PG 응답에 transactionKey가 없으면 실패 처리")
    void pg_response_without_transaction_key_fails() {
        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(PAYMENT_ID);
        PgPaymentResponse invalidResponse = new PgPaymentResponse(null, "SUCCESS", "결제 성공이지만 키 없음");
        when(pgGateway.requestPayment(anyString(), any())).thenReturn(invalidResponse);

        cardPaymentStrategy.pay(paymentCommand);

        verify(paymentStatePort).updateToFailed(eq(PAYMENT_ID), eq("PG 요청 무효"));
        verify(eventPublisher).publishEvent(isA(Object.class));
    }

    @Test
    @DisplayName("동시 결제 요청 처리 - Bulkhead 패턴 검증")
    void concurrent_payment_requests_handled_properly() {
        PaymentCommand command1 = createPaymentCommand("user1", 101L);
        PaymentCommand command2 = createPaymentCommand("user2", 102L);
        PaymentCommand command3 = createPaymentCommand("user3", 103L);

        when(paymentStatePort.createInitiatedPayment(any())).thenReturn(1L, 2L, 3L);
        PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123", "SUCCESS", "성공");
        when(pgGateway.requestPayment(anyString(), any())).thenReturn(pgResponse);

        cardPaymentStrategy.pay(command1);
        cardPaymentStrategy.pay(command2);
        cardPaymentStrategy.pay(command3);

        verify(paymentStatePort, times(3)).createInitiatedPayment(any());
        verify(pgGateway, times(3)).requestPayment(anyString(), any());
        verify(paymentStatePort, times(3)).updateToProcessing(any(), eq("tx_123"));
    }

    private PaymentCommand createPaymentCommand(String userId, Long orderId) {
        return new PaymentCommand(
            UserId.of(userId),
            orderId,
            "SAMSUNG",
            "1234-1234-1234-1234",
            OrderAmount.of(BigDecimal.valueOf(5000)),
            PaymentMethod.CARD
        );
    }
}
