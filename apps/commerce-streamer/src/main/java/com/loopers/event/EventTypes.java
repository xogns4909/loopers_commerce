package com.loopers.event;

import java.util.Set;

public final class EventTypes {
    private EventTypes() {}

    public static final String PAYMENT_COMPLETED = "PaymentCompleted";
    public static final String PAYMENT_FAILED    = "PaymentFailed";
    public static final String ORDER_CREATED     = "OrderCreated";
    public static final String ORDER_FAILED      = "OrderFailed";
    public static final String PRODUCT_LIKED     = "ProductLiked";
    public static final String PRODUCT_UNLIKED   = "ProductUnliked";
    public static final String PRODUCT_VIEWED    = "ProductViewed";
    public static final String MESSAGE_SEND_REQUESTED = "MessageSendRequested";

    public static final Set<String> ALL = Set.of(
        PAYMENT_COMPLETED, PAYMENT_FAILED,
        ORDER_CREATED, ORDER_FAILED,
        PRODUCT_LIKED, PRODUCT_UNLIKED, PRODUCT_VIEWED,
        MESSAGE_SEND_REQUESTED
    );
}
