package com.loopers.infrastructure.payment.model;

import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentStatus;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;
    
    @Column(name = "transaction_id", nullable = true)
    private String transactionId;
    
    @Column(name = "transaction_key", nullable = true)
    private String transactionKey;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "reason", nullable = true)
    private String reason;

    protected PaymentEntity() {}

    public static PaymentEntity from(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.userId = payment.getUserId().value();
        entity.orderId = payment.getOrderId();
        entity.amount = payment.getAmount().value().longValue();
        entity.method = payment.getMethod();
        entity.transactionId = payment.getTransactionId();
        entity.transactionKey = payment.getTransactionKey();
        entity.status = payment.getStatus();
        entity.reason = payment.getFailureReason();
        return entity;
    }

    public Payment toModel() {
        return Payment.reconstruct(
            getId(),
            UserId.of(userId),
            orderId,
            new PaymentAmount(BigDecimal.valueOf(amount)),
            method,
            transactionId,
            transactionKey,
            status,
            reason
        );
    }

    public void updateTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public void updateReason(String reason) {
        this.reason = reason;
    }
}
