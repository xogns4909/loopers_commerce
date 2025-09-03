package com.loopers.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.loopers.event.GeneralEnvelopeEvent;

import java.util.Set;

public interface EventHandler {

    default Set<String> supportedTypes() {
        return Set.of();
    }

    default boolean canHandle(String eventType) {
        return supportedTypes().contains(eventType);
    }

    void handle(GeneralEnvelopeEvent envelope);
}
