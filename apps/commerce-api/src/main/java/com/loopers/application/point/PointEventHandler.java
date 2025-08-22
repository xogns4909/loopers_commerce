package com.loopers.application.point;

import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventHandler {

    private final PointService pointService;
    private final PaymentRepository paymentRepository;

    @EventListener
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Payment payment = paymentRepository.findById(event.paymentId())
            .orElse(null);

        if (payment != null && payment.getMethod() == PaymentMethod.POINT) {
            pointService.restore(event.userId(), payment.getAmount());
            log.info("포인트 복구 완료 - userId: {}, amount: {}",
                event.userId(), payment.getAmount().value());
        }

    }
}
