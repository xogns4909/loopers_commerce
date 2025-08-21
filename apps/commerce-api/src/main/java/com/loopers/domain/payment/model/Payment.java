package com.loopers.domain.payment.model;

import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

import java.math.BigDecimal;
import lombok.Setter;

@Getter
public class Payment {

    private final Long id;
    private final Long orderId;
    private final UserId userId;
    private final PaymentAmount amount;
    private final PaymentMethod method;

    @Setter
    private String transactionId;
    @Setter
    private String transactionKey;
    private PaymentStatus status;
    private String failureReason;

    private Payment(Long id, Long orderId, UserId userId, PaymentAmount amount,
        PaymentMethod method, String transactionId, String transactionKey,
        PaymentStatus status, String failureReason) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.transactionId = transactionId;
        this.transactionKey = transactionKey;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.failureReason = failureReason;
    }

    public static Payment create(UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        validate(userId, orderId, amount, method);
        return new Payment(null, orderId, userId, amount, method, null, null, PaymentStatus.PENDING, null);
    }


    public static Payment reconstruct(Long id, UserId userId, Long orderId, PaymentAmount amount,
        PaymentMethod method, String transactionId,
        String transactionKey, PaymentStatus status, String failureReason) {
        return new Payment(id, orderId, userId, amount, method, transactionId, transactionKey, status, failureReason);
    }

    private static void validate(UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        if (orderId == null) throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID가 지정되지 않았습니다.");
        if (method == null)   throw new CoreException(ErrorType.BAD_REQUEST, "결제 수단이 유효하지 않습니다.");
        if (userId == null)   throw new CoreException(ErrorType.BAD_REQUEST, "사용자 정보가 필요합니다.");
        if (amount == null || amount.value().compareTo(BigDecimal.ZERO) <= 0)
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다.");
    }

    public void startProcessing() {
        transitionTo(PaymentStatus.PROCESSING, null);
    }

    public void succeed(String txKey) {
        if (txKey != null && !txKey.isBlank()) this.transactionKey = txKey;
        transitionTo(PaymentStatus.SUCCESS, null);
    }

    public void fail(String reason, String txKey) {
        if (txKey != null && !txKey.isBlank()) this.transactionKey = txKey;
        transitionTo(PaymentStatus.FAILED, reason != null ? reason : "결제 실패");
    }

    private void transitionTo(PaymentStatus newStatus, String reason) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                String.format("결제 상태를 %s에서 %s로 변경할 수 없습니다", this.status, newStatus));
        }
        this.status = newStatus;
        this.failureReason = newStatus.isFailed() ? reason : null;
    }

    public boolean isPending()    { return status == PaymentStatus.PENDING; }
    public boolean isProcessing() { return status == PaymentStatus.PROCESSING; }
    public boolean isCompleted()  { return status.isCompleted(); }
    public boolean isFailed()     { return status.isFailed(); }
}
