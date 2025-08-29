package com.loopers.application.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.strategy.PaymentStrategy;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    private final PaymentStatePort paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CARD;
    }

    @Override
    public void pay(PaymentCommand cmd) {
        Long paymentId = paymentService.createInitiatedPayment(cmd); // PENDING
        log.info("결제 PENDING 생성 - paymentId: {}, orderId: {}", paymentId, cmd.orderId());
        try {
            PgPaymentRequest req = PgPaymentRequest.of("ORDER_" + cmd.orderId(), cmd.CardType(), cmd.CardNo(),
                cmd.amount().value().longValue(), callbackUrl);
            PgPaymentResponse resp = pg.requestPayment(cmd.userId().value(), req);

            if (resp.transactionKey() == null || resp.transactionKey().isEmpty()) {
                publishFailedEvent(cmd, paymentId, "PG 요청 무효","PG 응답에 transactionKey가 없습니다");
            }

            paymentService.updateToProcessing(paymentId, resp.transactionKey());
            log.info("PG 접수 성공 - paymentId={}, txKey={}", paymentId, resp.transactionKey());
        } catch (Exception e) {
            publishFailedEvent(cmd, paymentId, "PG 요청 무효", e.getMessage());
            throw  e;
        }
    }

    private void publishFailedEvent(PaymentCommand cmd, Long paymentId, String reason, String message) {

        paymentService.updateToFailed(paymentId, reason);

        eventPublisher.publishEvent(new PaymentFailedEvent(
            paymentId, cmd.orderId(), cmd.userId(), reason, message
        ));
    }
}
