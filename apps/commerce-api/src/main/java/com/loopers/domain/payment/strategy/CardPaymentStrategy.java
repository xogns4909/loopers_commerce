package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentDataService;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.infrastructure.payment.pg.PgClient;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    private final PgClient pgClient;
    private final PaymentDataService paymentDataService;

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

        PgPaymentResponse resp = requestPg(cmd);

        if (resp != null && resp.transactionKey() != null && !resp.transactionKey().isBlank()) {
            paymentDataService.updateToProcessing(paymentId, resp.transactionKey());
        } else {
            paymentDataService.updateToFailed(paymentId, "PG 응답 오류: txKey 누락/요청 실패");
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 처리 실패(PG)");
        }
    }

    protected PgPaymentResponse requestPg(PaymentCommand cmd) {

        long amount = cmd.amount().value().longValue();

        PgPaymentRequest req = PgPaymentRequest.of(
            ORDER_ID_PREFIX + cmd.orderId(),
            amount,
            callbackUrl
        );
        return pgClient.requestPayment(cmd.userId().value(), req).data();
    }
}
