package com.loopers.domain.product.event;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class StockShortageEvent {
    private final Long productId;
    private final String productName;
    private final int currentStock;
    private final int requestedQuantity;
    private final List<String> cacheKeys;
    
    public static StockShortageEvent of(Long productId, String productName, 
                                        int currentStock, int requestedQuantity,
                                        List<String> cacheKeys) {
        return StockShortageEvent.builder()
            .productId(productId)
            .productName(productName)
            .currentStock(currentStock)
            .requestedQuantity(requestedQuantity)
            .cacheKeys(cacheKeys)
            .build();
    }
}
