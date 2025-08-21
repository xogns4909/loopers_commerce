package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentStatus;
import com.loopers.infrastructure.payment.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByTransactionKey(String transactionKey);
    
    Optional<PaymentEntity> findByTransactionId(String transactionId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    boolean existsByOrderIdAndMethodAndStatus(Long orderId, PaymentMethod method, PaymentStatus status);

    List<PaymentEntity> findByStatusOrderByCreatedAtAsc(PaymentStatus status);
}
