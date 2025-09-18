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
    
    // 캐시 무효화 관련 이벤트
    public static final String STOCK_SHORTAGE    = "StockShortage";
    public static final String PRODUCT_UPDATED   = "ProductUpdated";
    public static final String PRICE_CHANGED     = "PriceChanged";
    public static final String INVENTORY_UPDATED = "InventoryUpdated";

    public static final Set<String> ALL = Set.of(
        PAYMENT_COMPLETED, PAYMENT_FAILED,
        ORDER_CREATED, ORDER_FAILED,
        PRODUCT_LIKED, PRODUCT_UNLIKED, PRODUCT_VIEWED,
        MESSAGE_SEND_REQUESTED,
        STOCK_SHORTAGE, PRODUCT_UPDATED, PRICE_CHANGED, INVENTORY_UPDATED
    );
    

    public static final Set<String> METRIC_EVENTS = Set.of(
        PRODUCT_VIEWED, PRODUCT_LIKED, PRODUCT_UNLIKED,
        ORDER_CREATED
    );
    
    // 캐시 무효화 대상 이벤트
    public static final Set<String> CACHE_EVICTION_EVENTS = Set.of(
        STOCK_SHORTAGE, PRODUCT_UPDATED, PRICE_CHANGED, INVENTORY_UPDATED
    );
}
