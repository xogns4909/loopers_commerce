package com.loopers.application.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentStatus;
import com.loopers.domain.payment.strategy.PaymentStrategyFactory;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory strategyFactory;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;



    @Override
    public void pay(PaymentCommand command) {
        log.info("결제 시작 - orderId: {}, method: {}, amount: {}",
            command.orderId(), command.paymentMethod(), command.amount());
        strategyFactory.getStrategy(command.paymentMethod()).pay(command);
    }

    @Transactional
    @Override
    public Payment processCallback(PaymentCallbackRequest req) {
        Payment payment = paymentRepository.findByTransactionKey(req.transactionKey())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다"));

        PaymentStatus mapped = mapPgStatus(req.status());


        switch (mapped) {
            case SUCCESS -> payment.succeed(req.transactionKey());
            case FAILED  -> payment.fail(req.reason(), req.transactionKey());
            case PROCESSING -> payment.startProcessing();
        }

        Payment saved = paymentRepository.save(payment);


        if (saved.isCompleted()) {
            publisher.publishEvent(PaymentCompletedEvent.of(saved.getId(), saved.getOrderId(),
                saved.getUserId(), saved.getTransactionKey()));
        } else if (saved.isFailed()) {
            publisher.publishEvent(PaymentFailedEvent.of(saved.getId(), saved.getOrderId(),
                saved.getUserId(), saved.getFailureReason(), saved.getTransactionKey()));
        }

        return saved;
    }

    private PaymentStatus mapPgStatus(String pgStatus) {
        if (pgStatus == null) return PaymentStatus.FAILED;
        return switch (pgStatus.toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAILED" -> PaymentStatus.FAILED;
            case "PENDING" -> PaymentStatus.PENDING;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    @Override
    public Payment findByTransactionKey(String transactionKey) {
        return paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다"));
    }


}
