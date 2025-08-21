package com.loopers.domain.payment.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.order.PaymentCommand;
import com.loopers.application.payment.PaymentStateServiceImpl;
import com.loopers.application.payment.strategy.CardPaymentStrategy;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.application.payment.PaymentServiceImpl;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import feign.FeignException;
import feign.Request;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardPaymentStrategy 단위 테스트")
class CardPaymentStrategyTest {

    @Mock
    private PgPaymentGateway pg;

    @Mock
    private PaymentStateServiceImpl paymentService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CardPaymentStrategy cardPaymentStrategy;

    private PaymentCommand paymentCommand;
    private final Long PAYMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardPaymentStrategy, "callbackUrl", "http://localhost:8080/api/v1/payments/callback");
        
        paymentCommand = PaymentCommand.builder()
            .userId(UserId.of("testUser"))
            .orderId(100L)
            .amount(OrderAmount.of(BigDecimal.valueOf(10000)))
            .paymentMethod(PaymentMethod.CARD)
            .build();
    }

    @Nested
    @DisplayName("결제 수단 지원 확인")
    class SupportsTest {

        @Test
        @DisplayName("CARD 결제 수단 지원함")
        void supports_card() {
            boolean result = cardPaymentStrategy.supports(PaymentMethod.CARD);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("POINT 결제 수단 지원하지 않음")
        void not_supports_point() {
            boolean result = cardPaymentStrategy.supports(PaymentMethod.POINT);
            
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("결제 요청")
    class PayTest {

        @Test
        @DisplayName("PG 응답 성공 시 결제 처리중 상태로 업데이트")
        void pg_success_update_to_processing() {
            // given
            when(paymentService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
            
            PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123456", "SUCCESS", "결제 요청 성공");
            when(pg.requestPayment(eq("testUser"), any(PgPaymentRequest.class)))
                .thenReturn(pgResponse);

            // when
            cardPaymentStrategy.pay(paymentCommand);

            // then
            verify(paymentService).createInitiatedPayment(paymentCommand);
            verify(paymentService).updateToProcessing(PAYMENT_ID, "tx_123456");
            
            // PG 요청 파라미터 검증
            ArgumentCaptor<PgPaymentRequest> requestCaptor = ArgumentCaptor.forClass(PgPaymentRequest.class);
            verify(pg).requestPayment(eq("testUser"), requestCaptor.capture());
            
            PgPaymentRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.orderId()).isEqualTo("ORDER_100");
            assertThat(capturedRequest.amount()).isEqualTo("10000");
            assertThat(capturedRequest.callbackUrl()).isEqualTo("http://localhost:8080/api/v1/payments/callback");
        }

        @Test
        @DisplayName("PG 응답에 transactionKey가 없으면 실패 이벤트 발행")
        void pg_response_without_transaction_key_publishes_failed_event() {
            // given
            when(paymentService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
            
            PgPaymentResponse pgResponse = new PgPaymentResponse(null, "SUCCESS", "결제 요청 성공");
            when(pg.requestPayment(eq("testUser"), any(PgPaymentRequest.class)))
                .thenReturn(pgResponse);

            // when
            cardPaymentStrategy.pay(paymentCommand);

            // then
            ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            PaymentFailedEvent failedEvent = eventCaptor.getValue();
            assertThat(failedEvent.paymentId()).isEqualTo(PAYMENT_ID);
            assertThat(failedEvent.orderId()).isEqualTo(100L);
            assertThat(failedEvent.reason()).isEqualTo("PG 요청 무효");
        }

        @Test
        @DisplayName("PG 네트워크 에러 시 실패 이벤트 발행")
        void pg_network_error_publishes_failed_event() {
            // given
            when(paymentService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
            
            Request request = Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
            when(pg.requestPayment(eq("testUser"), any(PgPaymentRequest.class)))
                .thenThrow(new FeignException.InternalServerError("서버 오류", request, null, Collections.emptyMap()));

            // when
            cardPaymentStrategy.pay(paymentCommand);

            // then
            ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            PaymentFailedEvent failedEvent = eventCaptor.getValue();
            assertThat(failedEvent.paymentId()).isEqualTo(PAYMENT_ID);
            assertThat(failedEvent.orderId()).isEqualTo(100L);
            assertThat(failedEvent.reason()).isEqualTo("PG 요청 무효");
        }

        @Test
        @DisplayName("일반적인 예외 발생 시 실패 이벤트 발행")
        void general_exception_publishes_failed_event() {
            // given
            when(paymentService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
            when(pg.requestPayment(eq("testUser"), any(PgPaymentRequest.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

            // when
            cardPaymentStrategy.pay(paymentCommand);

            // then
            ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            PaymentFailedEvent failedEvent = eventCaptor.getValue();
            assertThat(failedEvent.paymentId()).isEqualTo(PAYMENT_ID);
            assertThat(failedEvent.orderId()).isEqualTo(100L);
            assertThat(failedEvent.reason()).isEqualTo("PG 요청 무효");
        }
    }
}
