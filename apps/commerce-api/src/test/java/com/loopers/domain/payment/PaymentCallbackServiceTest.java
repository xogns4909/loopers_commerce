package com.loopers.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.payment.PaymentServiceImpl;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentStatus;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCallbackService 단위 테스트")
class PaymentCallbackServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentCallbackService;

    private Payment payment;
    private final String TRANSACTION_KEY = "tx_123456";

    @BeforeEach
    void setUp() {
        payment = Payment.reconstruct(
            1L,
            UserId.of("testUser"),
            100L,
            new PaymentAmount(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD,
            "tx_123456",
            TRANSACTION_KEY,
            PaymentStatus.PROCESSING,
            "결제 처리 중"
        );
    }

    @Nested
    @DisplayName("콜백 처리")
    class ProcessCallbackTest {

        @Test
        @DisplayName("성공 콜백 시 결제 완료 이벤트 발행")
        void success_callback_publishes_completed_event() {
            // given
            when(paymentRepository.findByTransactionKey(TRANSACTION_KEY))
                .thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            PaymentCallbackRequest request = new PaymentCallbackRequest(
                TRANSACTION_KEY,
                "100",
                "SUCCESS", 
                "10000",
                "결제 완료",
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // when
            Payment result = paymentCallbackService.processCallback(request);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

            ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            PaymentCompletedEvent completedEvent = eventCaptor.getValue();
            assertThat(completedEvent.paymentId()).isEqualTo(1L);
            assertThat(completedEvent.orderId()).isEqualTo(100L);
            assertThat(completedEvent.transactionKey()).isEqualTo(TRANSACTION_KEY);
        }

        @Test
        @DisplayName("실패 콜백 시 결제 실패 이벤트 발행")
        void failed_callback_publishes_failed_event() {
            // given
            when(paymentRepository.findByTransactionKey(TRANSACTION_KEY))
                .thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            PaymentCallbackRequest request = new PaymentCallbackRequest(
                TRANSACTION_KEY,
                "100",
                "FAILED",
                "10000", 
                "카드 한도 초과",
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // when
            Payment result = paymentCallbackService.processCallback(request);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.getFailureReason()).isEqualTo("카드 한도 초과");

            ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            PaymentFailedEvent failedEvent = eventCaptor.getValue();
            assertThat(failedEvent.paymentId()).isEqualTo(1L);
            assertThat(failedEvent.orderId()).isEqualTo(100L);
            assertThat(failedEvent.transactionKey()).isEqualTo(TRANSACTION_KEY);
            assertThat(failedEvent.reason()).isEqualTo("카드 한도 초과");
        }

        @Test
        @DisplayName("존재하지 않는 transactionKey로 콜백 시 예외 발생")
        void not_found_transaction_key_throws_exception() {
            // given
            when(paymentRepository.findByTransactionKey("invalid_key"))
                .thenReturn(Optional.empty());

            PaymentCallbackRequest request = new PaymentCallbackRequest(
                "invalid_key",
                "100", 
                "SUCCESS",
                "10000",
                "결제 완료",
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // when & then
            assertThatThrownBy(() -> paymentCallbackService.processCallback(request))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("결제 조회")
    class FindByTransactionKeyTest {

        @Test
        @DisplayName("transactionKey로 결제 조회 성공")
        void find_by_transaction_key_success() {
            // given
            when(paymentRepository.findByTransactionKey(TRANSACTION_KEY))
                .thenReturn(Optional.of(payment));

            // when
            Payment result = paymentCallbackService.findByTransactionKey(TRANSACTION_KEY);

            // then
            assertThat(result).isEqualTo(payment);
            assertThat(result.getTransactionKey()).isEqualTo(TRANSACTION_KEY);
        }

        @Test
        @DisplayName("존재하지 않는 transactionKey 조회 시 예외 발생")
        void find_by_invalid_transaction_key_throws_exception() {
            // given
            when(paymentRepository.findByTransactionKey("invalid_key"))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentCallbackService.findByTransactionKey("invalid_key"))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다");
        }
    }
}
