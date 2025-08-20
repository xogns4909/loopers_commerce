package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Component
public class PointPaymentStrategy implements PaymentStrategy {

    private final PointService pointService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean supports(PaymentMethod method) { return method == PaymentMethod.POINT; }

    @Override
    @Transactional
    public void pay(PaymentCommand cmd) {

        if (paymentRepository.existsCompleted(cmd.orderId(), PaymentMethod.POINT)) return;

        PaymentAmount amount = PaymentAmount.from(cmd.amount());
        if (!pointService.hasEnough(cmd.userId(), amount)) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        pointService.deduct(cmd.userId(), amount);

        Payment payment = Payment.create(cmd.userId(), cmd.orderId(), amount, PaymentMethod.POINT);
        String txKey = generatePointTransactionKey(cmd.orderId(), cmd.userId().value());
        Payment completed = payment.withTransactionKey(txKey).completePayment("포인트 결제 완료");
        Payment saved = paymentRepository.save(completed);

        eventPublisher.publishEvent(PaymentCompletedEvent.of(saved.getId(), saved.getOrderId(), saved.getUserId(), txKey));
    }

    private String generatePointTransactionKey(Long orderId, String userId) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("POINT_%s_%s_%s", orderId, userId.substring(0, Math.min(8, userId.length())), ts);
    }
}
