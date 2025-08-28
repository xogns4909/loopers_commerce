package com.loopers.infrastructure.event;

public enum EventType {
    PAYMENT_COMPLETED("PaymentCompleted"),
    PAYMENT_FAILED("PaymentFailed"),
    ORDER_CREATED("OrderCreated"),
    ORDER_FAILED("OrderFailed"),
    PRODUCT_LIKED("ProductLiked"),
    PRODUCT_UNLIKED("ProductUnliked"),
    MESSAGE_SEND_REQUESTED("MessageSendRequested");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
