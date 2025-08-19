package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.point.service.PointService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
public class PointPaymentStrategy implements PaymentStrategy {

    private final PointService pointService;
    private final PaymentRepository paymentRepository;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.POINT;
    }

    @Override
    @Transactional
    public void pay(PaymentCommand command) {
        PaymentAmount amount = PaymentAmount.from(command.amount());

        if (!pointService.hasEnough(command.userId(), amount)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

        pointService.deduct(command.userId(), amount);

        Payment payment = Payment.create(command.userId(), command.orderId(), amount, command.paymentMethod());


        String pointTransactionKey = generatePointTransactionKey(command.orderId(), command.userId().value());
        Payment completedPayment = payment
            .withTransactionKey(pointTransactionKey)
            .completePayment("포인트 결제 완료");

        paymentRepository.save(completedPayment);
    }
    
    private String generatePointTransactionKey(Long orderId, String userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("POINT_%s_%s_%s", orderId, userId.substring(0, Math.min(8, userId.length())), timestamp);
    }
}
