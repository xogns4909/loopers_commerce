package com.loopers.interfaces.consumer;

public interface EventHandler {
    boolean canHandle(String eventType);
    void handle(EventEnvelopeProcessor.GeneralEnvelopeEvent envelope);
}
