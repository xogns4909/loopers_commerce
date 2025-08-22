package com.loopers.domain.order.model;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    FAILED;

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED;
    }
    
    public boolean canPayComplete() {
        return this == PENDING;
    }
    
    public boolean canPayFail() {
        return this == PENDING;
    }
}
