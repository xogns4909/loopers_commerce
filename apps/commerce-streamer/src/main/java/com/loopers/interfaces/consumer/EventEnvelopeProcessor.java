package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEnvelopeProcessor {
    
    private final ObjectMapper objectMapper;
    private final EventHandlerResolver handlerResolver;
    
    public void process(String eventJson) {
        GeneralEnvelopeEvent envelope = parseEnvelope(eventJson);
        EventHandler handler = handlerResolver.resolve(envelope.type());
        handler.handle(envelope);
    }
    
    private GeneralEnvelopeEvent parseEnvelope(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, GeneralEnvelopeEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse envelope", e);
        }
    }
    
    public record GeneralEnvelopeEvent(
        String messageId,
        String type,
        Object payload,
        String occurredAt,
        String source,
        String correlationId
    ) {}
}
