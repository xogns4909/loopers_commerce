package com.loopers.domain.payment;

import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionKey(String transactionKey);

    List<Payment> findByUserId(UserId userId);

    void updateToProcessing(Long paymentId, String txKey);
    void updateToFailed(Long paymentId, String reason);

    boolean existsCompleted(Long orderId, PaymentMethod method);

    List<Payment> findPending();

    void updateToCompleted(Long paymentId, String transactionKey);
}
