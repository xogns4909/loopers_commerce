package com.loopers.event;


public enum EventType {
    PAYMENT_COMPLETED("PaymentCompleted"),
    PAYMENT_FAILED("PaymentFailed"),
    ORDER_CREATED("OrderCreated"),
    ORDER_FAILED("OrderFailed"),
    PRODUCT_LIKED("ProductLiked"),
    PRODUCT_UNLIKED("ProductUnliked"),
    PRODUCT_VIEWED("ProductViewed"),
    MESSAGE_SEND_REQUESTED("MessageSendRequested"),
    STOCK_SHORTAGE("StockShortage"),
    PRODUCT_UPDATED("ProductUpdated"),
    PRICE_CHANGED("PriceChanged"),
    INVENTORY_UPDATED("InventoryUpdated");

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

    public static EventType fromString(String value) {
        for (EventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }
}
