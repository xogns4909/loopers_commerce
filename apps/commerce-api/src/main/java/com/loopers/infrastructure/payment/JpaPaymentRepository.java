package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity,Long> {

}
