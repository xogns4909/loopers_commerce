package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDataService {
    
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Long createInitiatedPayment(PaymentCommand cmd) {
        PaymentAmount amount = PaymentAmount.from(cmd.amount());
        Payment payment = Payment.initiated(cmd.userId(), cmd.orderId(), amount, cmd.paymentMethod());
        Payment saved = paymentRepository.save(payment);
        
        log.info("결제 생성 완료 - paymentId: {}, orderId: {}, amount: {}", 
            saved.getId(), cmd.orderId(), amount.value());
        
        return saved.getId();
    }
    
    @Transactional
    public void updateToProcessing(Long paymentId, String transactionKey) {
        paymentRepository.updateToProcessing(paymentId, transactionKey);
        log.info("결제 처리중 상태 업데이트 - paymentId: {}, txKey: {}", paymentId, transactionKey);
    }



}
