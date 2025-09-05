package com.loopers.domain.product.event;

import com.loopers.domain.user.model.UserId;
import java.time.Instant;
import java.util.UUID;

public record ProductViewedEvent(
    String eventId,
    Long productId,
    String userId,
    String viewType,
    Instant viewedAt
) {
    

    public static ProductViewedEvent ofSingleView(Long productId, UserId userId) {
        return new ProductViewedEvent(
            UUID.randomUUID().toString(),
            productId,
            userId.value(),
            "SINGLE",
            Instant.now()
        );
    }
    

    public static ProductViewedEvent ofListView(Long productId, UserId userId) {
        return new ProductViewedEvent(
            UUID.randomUUID().toString(),
            productId,
            userId.value(),
            "LIST",
            Instant.now()
        );
    }
}
