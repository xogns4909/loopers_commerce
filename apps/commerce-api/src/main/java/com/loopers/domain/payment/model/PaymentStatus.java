package com.loopers.domain.payment.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED;
    
    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == SUCCESS || newStatus == FAILED;
            case PROCESSING -> newStatus == SUCCESS || newStatus == FAILED;
            case SUCCESS, FAILED -> false;
        };
    }
    
    public boolean isCompleted() {
        return this == SUCCESS;
    }
    
    public boolean isFailed() {
        return this == FAILED;
    }
    
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }
}
