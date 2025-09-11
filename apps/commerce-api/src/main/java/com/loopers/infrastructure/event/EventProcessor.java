package com.loopers.infrastructure.event;

public interface EventProcessor {
    boolean canProcess(EventType eventType);
    void process(Object payload, String messageId, String correlationId);
}
