package com.loopers.domain.like.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductUnlikedEvent(
    String eventId,
    Long productId,
    String userId,
    String correlationId,
    LocalDateTime occurredAt
) {
    public static ProductUnlikedEvent of(Long productId, String userId, String correlationId) {
        return new ProductUnlikedEvent(
            UUID.randomUUID().toString(),
            productId,
            userId,
            correlationId,
            LocalDateTime.now()
        );
    }
}
