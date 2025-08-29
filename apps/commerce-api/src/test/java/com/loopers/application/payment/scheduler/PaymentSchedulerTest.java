package com.loopers.application.payment.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentStatus;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentStatusResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 상태 복구 스케줄러 테스트")
class PaymentSchedulerTest {

    @Mock
    private PaymentStatePort paymentStatePort;

    @Mock
    private PgPaymentGateway pgGateway;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentScheduler paymentScheduler;

    private Payment pendingPayment;
    private final Long PAYMENT_ID = 1L;
    private final Long ORDER_ID = 100L;
    private final UserId USER_ID = UserId.of("testUser");

    @BeforeEach
    void setUp() {
        pendingPayment = createPendingPayment();
    }

    @Test
    @DisplayName("PENDING 결제가 없으면 조기 종료")
    void no_pending_payments_early_return() {
        // given: PENDING 결제가 없는 상황
        when(paymentStatePort.loadPending()).thenReturn(List.of());

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: PG 조회 없이 종료
        verify(pgGateway, org.mockito.Mockito.never()).getPaymentByOrderId(any(), any());
    }

    @Test
    @DisplayName("콜백 누락된 SUCCESS 결제가 스케줄러로 복구되는가")
    void scheduler_recovers_missing_success_callback() {
        // given: PENDING 상태인 결제 (콜백이 안온 상황)
        when(paymentStatePort.loadPending()).thenReturn(List.of(pendingPayment));

        // PG 조회하면 실제로는 SUCCESS 상태
        PgPaymentStatusResponse successResponse = new PgPaymentStatusResponse(
            "tx_123456", "ORDER_100", "SUCCESS", "10000", null,
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenReturn(successResponse);

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: SUCCESS로 상태 변경되고 완료 이벤트 발행
        verify(paymentStatePort).updateToCompleted(PAYMENT_ID, "tx_123456");
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("콜백 누락된 FAILED 결제가 스케줄러로 복구되는가")
    void scheduler_recovers_missing_failed_callback() {
        // given: PENDING 상태인 결제
        when(paymentStatePort.loadPending()).thenReturn(List.of(pendingPayment));

        // PG 조회하면 실제로는 FAILED 상태
        PgPaymentStatusResponse failedResponse = new PgPaymentStatusResponse(
            "tx_failed_123", "ORDER_100", "FAILED", "10000", "한도 초과",
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenReturn(failedResponse);

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: FAILED로 상태 변경되고 실패 이벤트 발행
        verify(paymentStatePort).updateToFailed(PAYMENT_ID, "한도 초과");
        verify(eventPublisher).publishEvent(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("PG에서도 여전히 PENDING인 경우 상태 유지")
    void scheduler_keeps_pending_when_pg_still_pending() {
        // given: PENDING 상태인 결제
        when(paymentStatePort.loadPending()).thenReturn(List.of(pendingPayment));

        // PG 조회해도 여전히 PENDING 상태
        PgPaymentStatusResponse pendingResponse = new PgPaymentStatusResponse(
            "tx_pending_123", "ORDER_100", "PENDING", "10000", null,
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenReturn(pendingResponse);

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: 상태 변경 없이 로그만 출력
        verify(paymentStatePort, org.mockito.Mockito.never()).updateToCompleted(any(), any());
        verify(paymentStatePort, org.mockito.Mockito.never()).updateToFailed(any(), any());
        verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
    }

    @Test
    @DisplayName("PG 상태 조회 실패 시 해당 결제를 실패 처리")
    void scheduler_fails_payment_when_pg_query_fails() {
        // given: PENDING 상태인 결제
        when(paymentStatePort.loadPending()).thenReturn(List.of(pendingPayment));

        // PG 조회가 실패하는 상황
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 서버 장애"));

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: 해당 결제를 실패 처리
        verify(paymentStatePort).updateToFailed(eq(PAYMENT_ID), any(String.class));
        verify(eventPublisher).publishEvent(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("알 수 없는 PG 상태에 대한 처리")
    void scheduler_handles_unknown_pg_status() {
        // given: PENDING 상태인 결제
        when(paymentStatePort.loadPending()).thenReturn(List.of(pendingPayment));

        // PG 조회 결과가 알 수 없는 상태
        PgPaymentStatusResponse unknownResponse = new PgPaymentStatusResponse(
            "tx_unknown_123", "ORDER_100", "UNKNOWN_STATUS", "10000", "알 수 없는 상태",
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenReturn(unknownResponse);

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: 상태 변경 없이 경고 로그만 출력
        verify(paymentStatePort, org.mockito.Mockito.never()).updateToCompleted(any(), any());
        verify(paymentStatePort, org.mockito.Mockito.never()).updateToFailed(any(), any());
    }

    @Test
    @DisplayName("PG 응답에 transactionKey가 없는 경우 기존 키 사용")
    void scheduler_uses_existing_key_when_pg_key_missing() {
        // given: PENDING 상태인 결제 (기존 transactionKey 보유)
        Payment paymentWithKey = createPendingPaymentWithTransactionKey("existing_tx_key");
        when(paymentStatePort.loadPending()).thenReturn(List.of(paymentWithKey));

        // PG 응답에 transactionKey가 없는 상황
        PgPaymentStatusResponse responseWithoutKey = new PgPaymentStatusResponse(
            null, "ORDER_100", "SUCCESS", "10000", null,  // transactionKey가 null
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_" + ORDER_ID))
            .thenReturn(responseWithoutKey);

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: 기존 transactionKey를 사용해서 상태 업데이트
        verify(paymentStatePort).updateToCompleted(PAYMENT_ID, "existing_tx_key");
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("복수 PENDING 결제 일괄 처리")
    void scheduler_processes_multiple_pending_payments() {
        // given: 여러 PENDING 결제
        Payment payment1 = createPendingPayment(1L, 101L);
        Payment payment2 = createPendingPayment(2L, 102L);
        Payment payment3 = createPendingPayment(3L, 103L);

        when(paymentStatePort.loadPending()).thenReturn(List.of(payment1, payment2, payment3));

        // 각각 다른 결과로 응답
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_101"))
            .thenReturn(new PgPaymentStatusResponse("tx_101", "ORDER_101", "SUCCESS", "10000", null,
                LocalDateTime.now(), LocalDateTime.now()));
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_102"))
            .thenReturn(new PgPaymentStatusResponse("tx_102", "ORDER_102", "FAILED", "10000", "한도 초과",
                LocalDateTime.now(), LocalDateTime.now()));
        when(pgGateway.getPaymentByOrderId(USER_ID.value(), "ORDER_103"))
            .thenReturn(new PgPaymentStatusResponse("tx_103", "ORDER_103", "PENDING", "10000", null,
                LocalDateTime.now(), LocalDateTime.now()));

        // when: 스케줄러 실행
        paymentScheduler.sweepPending();

        // then: 각각 적절히 처리됨
        verify(paymentStatePort).updateToCompleted(1L, "tx_101");
        verify(paymentStatePort).updateToFailed(2L, "한도 초과");


        verify(eventPublisher).publishEvent(isA(PaymentCompletedEvent.class));
        verify(eventPublisher).publishEvent(isA(PaymentFailedEvent.class));
    }

    private Payment createPendingPayment() {
        return createPendingPayment(PAYMENT_ID, ORDER_ID);
    }

    private Payment createPendingPayment(Long paymentId, Long orderId) {
        return Payment.reconstruct(
            paymentId,
            USER_ID,
            orderId,
            new PaymentAmount(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD,
            null,
            null,
            PaymentStatus.PENDING,
            null
        );
    }

    private Payment createPendingPaymentWithTransactionKey(String transactionKey) {
        return Payment.reconstruct(
            PAYMENT_ID,
            USER_ID,
            ORDER_ID,
            new PaymentAmount(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD,
            null,
            transactionKey,
            PaymentStatus.PENDING,
            null
        );
    }
}
