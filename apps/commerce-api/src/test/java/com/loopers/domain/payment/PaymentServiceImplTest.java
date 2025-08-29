package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.application.payment.PaymentStateServiceImpl;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class paymentStateServiceimpltest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentStateServiceImpl paymentStateService;

    private PaymentCommand paymentCommand;

    @BeforeEach
    void setUp() {
        paymentCommand = PaymentCommand.builder()
            .userId(UserId.of("testUser"))
            .orderId(1L)
            .amount(OrderAmount.of(BigDecimal.valueOf(10000)))
            .paymentMethod(PaymentMethod.CARD)
            .build();
    }

    @Test
    @DisplayName("결제 생성 성공")
    void createInitiatedPayment_success() {
        // given
        Payment mockPayment = Payment.create(
            paymentCommand.userId(), 
            paymentCommand.orderId(), 
            PaymentAmount.from(paymentCommand.amount()), 
            paymentCommand.paymentMethod()
        );

        Payment savedPayment = Payment.reconstruct(
            99L, 
            mockPayment.getUserId(), 
            mockPayment.getOrderId(), 
            mockPayment.getAmount(), 
            mockPayment.getMethod(),
            mockPayment.getTransactionId(),
            mockPayment.getTransactionKey(),
            mockPayment.getStatus(),
            mockPayment.getFailureReason()
        );
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // when
        Long paymentId = paymentStateService.createInitiatedPayment(paymentCommand);

        // then
        assertThat(paymentId).isEqualTo(99L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 처리중 상태 업데이트")
    void updateToProcessing_success() {
        // given
        Long paymentId = 1L;
        String transactionKey = "tx_123456";

        // when
        paymentStateService.updateToProcessing(paymentId, transactionKey);

        // then
        verify(paymentRepository).updateToProcessing(paymentId, transactionKey);
    }


}
