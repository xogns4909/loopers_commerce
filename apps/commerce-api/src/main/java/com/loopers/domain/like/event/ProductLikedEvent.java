package com.loopers.domain.like.event;

public record ProductLikedEvent(String eventId, Long productId, String userId, String correlationId,
                                java.time.Instant occurredAt) {

    public static ProductLikedEvent of(Long productId, String userId, String correlationId) {
        return new ProductLikedEvent(java.util.UUID.randomUUID().toString(), productId, userId, correlationId,
            java.time.Instant.now());
    }
}

