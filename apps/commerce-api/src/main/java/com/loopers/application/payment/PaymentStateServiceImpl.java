package com.loopers.application.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatePort;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStateServiceImpl implements PaymentStatePort {

    private final PaymentRepository paymentRepository;

    @Transactional
    @Override
    public Long createInitiatedPayment(PaymentCommand cmd) {
        Payment payment = Payment.create(cmd.userId(), cmd.orderId(), PaymentAmount.from(cmd.amount()), cmd.paymentMethod());
        return paymentRepository.save(payment).getId();
    }

    @Transactional
    @Override
    public void updateToProcessing(Long id, String transactionKey) {
        paymentRepository.updateToProcessing(id, transactionKey);
    }

    @Transactional
    @Override
    public void updateToCompleted(Long id, String transactionKey) {
        paymentRepository.updateToCompleted(id, transactionKey);
    }

    @Transactional
    @Override
    public void updateToFailed(Long id, String reason) {
        paymentRepository.updateToFailed(id, reason);
    }

    @Override
    public List<Payment> loadPending() {
        return paymentRepository.findPending();
    }
}
