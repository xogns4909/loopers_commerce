package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.payment.model.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(PaymentEntity.from(payment)).toModel();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaPaymentRepository.findById(id)
                .map(PaymentEntity::toModel);
    }

    @Override
    public Optional<Payment> findByTransactionKey(String transactionKey) {
        return jpaPaymentRepository.findByTransactionKey(transactionKey)
                .map(PaymentEntity::toModel);
    }



    @Override
    public List<Payment> findByUserId(UserId userId) {
        return jpaPaymentRepository.findByUserIdOrderByCreatedAtDesc(userId.value())
                .stream()
                .map(PaymentEntity::toModel)
                .toList();
    }
}
