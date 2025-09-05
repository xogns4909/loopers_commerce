package com.loopers.infrastructure.event;

import java.time.Instant;

public record Envelope<T>(
    String messageId,
    String type,
    T payload,
    Instant occurredAt,
    String source,
    String correlationId
) {
    public static <T> Envelope<T> create(String messageId, String type, T payload,
        String source, String correlationId) {
        return new Envelope<>(messageId, type, payload, Instant.now(), source, correlationId);
    }
}
