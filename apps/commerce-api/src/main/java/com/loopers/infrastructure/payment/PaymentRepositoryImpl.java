package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentStatus;
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

    @Override
    public void updateToProcessing(Long paymentId, String txKey) {
        jpaPaymentRepository.findById(paymentId).ifPresent(entity -> {
            entity.updateTransactionKey(txKey);
            entity.updateStatus(PaymentStatus.PROCESSING);
            jpaPaymentRepository.save(entity);
        });
    }

    @Override
    public void updateToFailed(Long paymentId, String reason) {
        jpaPaymentRepository.findById(paymentId).ifPresent(entity -> {
            entity.updateStatus(PaymentStatus.FAILED);
            entity.updateReason(reason);
            jpaPaymentRepository.save(entity);
        });
    }

    @Override
    public boolean existsCompleted(Long orderId, PaymentMethod method) {
        return jpaPaymentRepository.existsByOrderIdAndMethodAndStatus(
                orderId, method, PaymentStatus.SUCCESS);
    }

    @Override
    public List<Payment> findPending() {
        return jpaPaymentRepository.findByStatusOrderByCreatedAtAsc(PaymentStatus.PENDING)
            .stream().map(PaymentEntity::toModel).toList();
    }

    @Override
    public void updateToCompleted(Long paymentId, String txKey) {
        jpaPaymentRepository.findById(paymentId).ifPresent(e -> {
            if (txKey != null && !txKey.isBlank()) e.updateTransactionKey(txKey);
            e.updateStatus(PaymentStatus.SUCCESS);
            jpaPaymentRepository.save(e);
        });
    }
}
