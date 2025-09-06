package com.loopers.infrastructure.event;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum EventType {
    PAYMENT_COMPLETED("PaymentCompleted"),
    PAYMENT_FAILED("PaymentFailed"),
    ORDER_CREATED("OrderCreated"),
    ORDER_FAILED("OrderFailed"),
    PRODUCT_LIKED("ProductLiked"),
    PRODUCT_UNLIKED("ProductUnliked"),
    PRODUCT_VIEWED("ProductViewed"),
    MESSAGE_SEND_REQUESTED("MessageSendRequested"),
    STOCK_SHORTAGE("StockShortage");  // 재고 부족 이벤트 추가

    private final String value;

    EventType(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }
    
    public static EventType fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"확인되지 않은 이벤트 입니다."));
    }
}
