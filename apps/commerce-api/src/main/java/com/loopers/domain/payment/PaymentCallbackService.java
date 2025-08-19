package com.loopers.domain.payment;

import com.loopers.domain.payment.model.Payment;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;

    public Payment processCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByTransactionKey(request.transactionKey())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다"));
        
        Payment updatedPayment = payment.processCallback(request);
        return paymentRepository.save(updatedPayment);
    }

    @Transactional(readOnly = true)
    public Payment findByTransactionKey(String transactionKey) {
        return paymentRepository.findByTransactionKey(transactionKey)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다"));
    }
}
