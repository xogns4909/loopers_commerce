package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.model.Payment;
import com.loopers.infrastructure.payment.model.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(PaymentEntity.from(payment)).toModel();
    }
}
