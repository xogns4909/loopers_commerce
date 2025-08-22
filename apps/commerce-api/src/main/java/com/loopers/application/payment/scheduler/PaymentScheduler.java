package com.loopers.application.payment.scheduler;

import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.infrastructure.payment.pg.PgPaymentGateway;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentStatusResponse;
import com.loopers.support.error.CoreException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentStatePort paymentStatePort;
    private final PgPaymentGateway pgGateway;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${payment.scheduler.sweep.cron:0 */5 * * * *}")
    @Transactional
    public void sweepPending() {
        log.info("결제 PENDING 스윕 시작");

        List<Payment> pendingPayments = paymentStatePort.loadPending();
        if (pendingPayments.isEmpty()) return;

        for (Payment payment : pendingPayments) {
            try {
                PgPaymentStatusResponse statusResponse = pgGateway.getPaymentByOrderId(
                    payment.getUserId().value(),
                    "ORDER_" + payment.getOrderId()
                );

                String status = statusResponse.status() == null ? "" : statusResponse.status().toUpperCase();
                String txKey = (statusResponse.transactionKey() != null && !statusResponse.transactionKey().isBlank())
                    ? statusResponse.transactionKey()
                    : payment.getTransactionKey();

                switch (status) {
                    case "SUCCESS" -> {
                        paymentStatePort.updateToCompleted(payment.getId(), txKey);
                        eventPublisher.publishEvent(PaymentCompletedEvent.of(
                            payment.getId(), payment.getOrderId(), payment.getUserId(), txKey));
                        log.info("PENDING → SUCCESS 복구 완료 - paymentId={}, txKey={}", payment.getId(), txKey);
                    }
                    case "FAILED" -> {
                        String reason = safeReason(statusResponse.reason(), "PG 콜백: 실패");
                        paymentStatePort.updateToFailed(payment.getId(), reason);
                        eventPublisher.publishEvent(PaymentFailedEvent.of(
                            payment.getId(), payment.getOrderId(), payment.getUserId(), reason, statusResponse.transactionKey()));
                        log.info("PENDING → FAILED 복구 완료 - paymentId={}, reason={}", payment.getId(), reason);
                    }
                    case "PENDING" -> {
                        log.debug("여전히 PENDING - paymentId={}", payment.getId());
                    }
                    default -> {
                        String reason = safeReason(statusResponse.reason(), "PG 상태 미확인");
                        log.warn("UNKNOWN PG 상태 - paymentId={}, status={}, msg={}", payment.getId(), status, reason);
                    }
                }

            } catch (CoreException ex) {
                String reason = safeReason(ex.getMessage(), "PG 상태 조회 실패");
                paymentStatePort.updateToFailed(payment.getId(), reason);
                eventPublisher.publishEvent(PaymentFailedEvent.of(
                    payment.getId(), payment.getOrderId(), payment.getUserId(), reason, null));
                log.error("PENDING 복구 실패(CoreException) - paymentId={}, reason={}", payment.getId(), reason, ex);
            }
        }
    }

    private static String safeReason(String raw, String fallback) {
        return (raw == null || raw.isBlank()) ? fallback : raw;
    }
}
