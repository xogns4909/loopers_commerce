package com.loopers.application.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.strategy.PaymentStrategy;
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
    private final PaymentStatePort paymentService;
    private final ApplicationEventPublisher publisher;

    @Override
    public boolean supports(PaymentMethod method) { return method == PaymentMethod.POINT; }


    @Override
    @Transactional
    public void pay(PaymentCommand cmd) {

        if (paymentRepository.existsCompleted(cmd.orderId(), PaymentMethod.POINT)) return;


        Long paymentId = paymentService.createInitiatedPayment(cmd); // status=PENDING
        log.info("포인트 결제 PENDING 생성 - paymentId={}, orderId={}", paymentId, cmd.orderId());


        var amount = PaymentAmount.from(cmd.amount());
        if (!pointService.hasEnough(cmd.userId(), amount)) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        pointService.deduct(cmd.userId(), amount);


        String txKey = generatePointTxKey(cmd.orderId(), cmd.userId().value());
        paymentService.updateToCompleted(paymentId, txKey);


        publisher.publishEvent(
            com.loopers.domain.payment.event.PaymentCompletedEvent.of(
                paymentId, cmd.orderId(), cmd.userId(), txKey
            )
        );

    }

    private String generatePointTxKey(Long orderId, String userId) {
        String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("POINT_%s_%s_%s", orderId, userId.substring(0, Math.min(8, userId.length())), ts);
    }
}
