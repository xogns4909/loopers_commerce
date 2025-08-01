package com.loopers.infrastructure.payment.model;



import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {

    private String userId;
    private Long orderId;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    protected PaymentEntity() {}

    public static PaymentEntity from(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.userId = payment.getUserId().value();
        entity.orderId = payment.getOrderId();
        entity.amount = payment.getAmount().value().longValue();
        entity.method = payment.getMethod();
        return entity;
    }

    public Payment toModel() {
        return Payment.reconstruct(
            getId(),
            UserId.of(userId),
            orderId,
            new PaymentAmount(BigDecimal.valueOf(amount)),
            method
        );
    }
}
