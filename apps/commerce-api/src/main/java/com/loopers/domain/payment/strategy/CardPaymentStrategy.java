package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.infrastructure.payment.pg.PgClient;
import com.loopers.infrastructure.payment.pg.dto.PgApiResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@RequiredArgsConstructor
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    private final PgClient pgClient;
    private final PaymentRepository paymentRepository;
    private static final String ORDER_ID_PREFIX = "ORDER_";

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CARD;
    }

    @Override
    @Transactional
    public void pay(PaymentCommand command) {
        PaymentAmount amount = PaymentAmount.from(command.amount());
        Payment payment = Payment.create(command.userId(), command.orderId(), amount, command.paymentMethod());
        payment = paymentRepository.save(payment);

        try {
            PgPaymentRequest req = PgPaymentRequest.of(
                ORDER_ID_PREFIX + command.orderId(),
                amount.value().longValue(),
                callbackUrl
            );

           PgPaymentResponse resp = pgClient.requestPayment(command.userId().value(), req).data();
            String txKey = resp != null ? resp.transactionKey() : null;

            if (txKey == null || txKey.isBlank()) {
                Payment failed = payment.failPayment("PG 응답에 transactionKey 누락");
                paymentRepository.save(failed);
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답 오류: txKey 누락");
            }
            Payment pending = payment.withTransactionKey(txKey).startProcessing();
            paymentRepository.save(pending);


        } catch (Exception e) {
            Payment failed = payment.failPayment("결제 요청 실패: " + e.getMessage());
            paymentRepository.save(failed);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 처리 실패: " + e.getMessage());
        }
    }
}
