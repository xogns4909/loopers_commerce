package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentDataService;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    private final PgPaymentGateway pg;
    private final PaymentDataService paymentDataService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    @Override
    public boolean supports(PaymentMethod method) { return method == PaymentMethod.CARD; }

    @Override
    public void pay(PaymentCommand cmd) {
        Long paymentId = paymentDataService.createInitiatedPayment(cmd); // PENDING
        log.info("결제 PENDING 생성 - paymentId: {}, orderId: {}", paymentId, cmd.orderId());
        try {
            PgPaymentRequest req = PgPaymentRequest.of("ORDER_" + cmd.orderId(), cmd.amount().value().longValue(), callbackUrl);
            PgPaymentResponse resp = pg.requestPayment(cmd.userId().value(), req);

            if (resp.transactionKey() == null || resp.transactionKey().isEmpty()) {
                throw new IllegalStateException("PG 응답에 transactionKey가 없습니다");
            }

            paymentDataService.updateToProcessing(paymentId, resp.transactionKey());
            log.info("PG 접수 성공 - paymentId={}, txKey={}", paymentId, resp.transactionKey());
        } catch (Exception e) {
            publishFailedEvent(cmd, paymentId, "PG 요청 무효", e.getMessage());
        }
    }

    private void publishFailedEvent(PaymentCommand cmd, Long paymentId, String reason, String message) {

        paymentDataService.updateToFailed(paymentId, reason);

        eventPublisher.publishEvent(new PaymentFailedEvent(
            paymentId, cmd.orderId(), cmd.userId(), reason, message
        ));
    }
}
