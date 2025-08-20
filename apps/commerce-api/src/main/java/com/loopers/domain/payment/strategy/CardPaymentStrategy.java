package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentDataService;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.infrastructure.payment.pg.PgClient;
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

    private final PgClient pgClient;
    private final PaymentDataService paymentDataService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String ORDER_ID_PREFIX = "ORDER_";

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CARD;
    }

    @Override
    public void pay(PaymentCommand cmd) {
        Long paymentId = paymentDataService.createInitiatedPayment(cmd);
        log.info("결제 ID 생성 완료 - paymentId: {}, orderId: {}", paymentId, cmd.orderId());

        try {
            PgPaymentResponse resp = requestPg(cmd);

            if (resp != null && resp.transactionKey() != null && !resp.transactionKey().isBlank()) {
                paymentDataService.updateToProcessing(paymentId, resp.transactionKey());
            } else {
                publishFailedEvent(cmd, paymentId, "PG 응답 무효", "transactionKey가 없거나 비어있음");
            }
        } catch (Exception e) {
            publishFailedEvent(cmd, paymentId, "PG 서버 오류", e.getMessage());
        }
    }

    private void publishFailedEvent(PaymentCommand cmd, Long paymentId, String reason, String message) {

            PaymentFailedEvent event = new PaymentFailedEvent(
                paymentId,
                cmd.orderId(),
                cmd.userId(),
                reason,
                message
            );
            eventPublisher.publishEvent(event);


    }
    protected PgPaymentResponse requestPg(PaymentCommand cmd) {
        try {
            long amount = cmd.amount().value().longValue();

            PgPaymentRequest req = PgPaymentRequest.of(
                ORDER_ID_PREFIX + cmd.orderId(),
                amount,
                callbackUrl
            );
            return pgClient.requestPayment(cmd.userId().value(), req).data();
        } catch (Exception e) {
            log.error("PG 클라이언트 요청 실패 - orderId: {}, error: {}", cmd.orderId(), e.getMessage());
            return null;
        }
    }
}
