package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.model.Payment;
import java.util.List;

public interface PaymentStatePort {
    Long createInitiatedPayment(PaymentCommand cmd);
    void updateToProcessing(Long paymentId, String txKey);
    void updateToCompleted(Long paymentId, String txKey);
    void updateToFailed(Long paymentId, String reason);
    List<Payment> loadPending();
}
