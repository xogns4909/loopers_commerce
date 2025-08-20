package com.loopers.domain.payment.model;

import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.math.BigDecimal;

@Getter
public class Payment {

    private final Long id;
    private final Long orderId;
    private final UserId userId;
    private final PaymentAmount amount;
    private final PaymentMethod method;
    
    private String transactionId;
    private String transactionKey;
    private PaymentStatus status;
    private String reason;

    private Payment(Long id, Long orderId, UserId userId, PaymentAmount amount, 
                   PaymentMethod method, String transactionId, String transactionKey,
                   PaymentStatus status, String reason) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.transactionId = transactionId;
        this.transactionKey = transactionKey;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.reason = reason;
    }

    public static Payment create(UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        return new Payment(null, orderId, userId, amount, method, null, null, PaymentStatus.PENDING, null);
    }

    public static Payment initiated(UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        return new Payment(null, orderId, userId, amount, method, null, null, PaymentStatus.PENDING, null);
    }

    public static Payment reconstruct(Long id, UserId userId, Long orderId, PaymentAmount amount, 
                                    PaymentMethod method, String transactionId, PaymentStatus status, String reason) {
        return new Payment(id, orderId, userId, amount, method, transactionId, null, status, reason);
    }
    
    public static Payment reconstruct(Long id, UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        return reconstruct(id, userId, orderId, amount, method, null, PaymentStatus.PENDING, null);
    }

    public Payment withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }
    
    public Payment withTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
        return this;
    }
    
    public Payment startProcessing() {
        return updateStatus(PaymentStatus.PROCESSING, "결제 처리 시작");
    }
    
    public Payment completePayment(String reason) {
        return updateStatus(PaymentStatus.SUCCESS, reason != null ? reason : "결제 완료");
    }
    
    public Payment failPayment(String reason) {
        return updateStatus(PaymentStatus.FAILED, reason != null ? reason : "결제 실패");
    }

    public Payment processCallback(PaymentCallbackRequest request, ApplicationEventPublisher eventPublisher) {
        PaymentStatus newStatus = convertPgStatus(request.status());
        String callbackReason = request.reason() != null ? request.reason() : "PG 콜백 처리";
        
        Payment updatedPayment = updateStatus(newStatus, callbackReason);
        
        // 이벤트 발행
        if (updatedPayment.isCompleted()) {
            eventPublisher.publishEvent(PaymentCompletedEvent.of(
                this.id, this.orderId, this.userId, this.transactionKey
            ));
        } else if (updatedPayment.isFailed()) {
            eventPublisher.publishEvent(PaymentFailedEvent.of(
                this.id, this.orderId, this.userId, callbackReason, this.transactionKey
            ));
        }
        
        return updatedPayment;
    }
    
    private PaymentStatus convertPgStatus(String pgStatus) {
        return switch (pgStatus.toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAILED" -> PaymentStatus.FAILED;
            case "PENDING" -> PaymentStatus.PENDING;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }
    
    private Payment updateStatus(PaymentStatus newStatus, String reason) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                String.format("결제 상태를 %s에서 %s로 변경할 수 없습니다", this.status, newStatus));
        }
        
        this.status = newStatus;
        this.reason = reason;
        return this;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
    }
    
    public boolean isCompleted() {
        return status.isCompleted();
    }
    
    public boolean isFailed() {
        return status.isFailed();
    }
    
    public String getTransactionKey() {
        return transactionKey;
    }

    public void validatePaymentRequest() {
        if (amount == null || amount.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다");
        }
        
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 정보가 필요합니다");
        }
        
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 정보가 필요합니다");
        }
    }
}
