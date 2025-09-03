package com.loopers.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventBridge {

    private final ApplicationEventPublisher publisher;
    private final EnvelopeFactory envelopeFactory;
    private final EventProcessorComposite eventProcessorComposite;

    public void publishEvent(EventType eventType, Object payload) {
        Envelope<Object> envelope = envelopeFactory.create(eventType, payload);
        GeneralEnvelopeEvent generalEvent = GeneralEnvelopeEvent.from(envelope);
        
        publisher.publishEvent(generalEvent);
        eventProcessorComposite.process(
            envelope.type(), 
            envelope.payload(), 
            envelope.messageId(), 
            envelope.correlationId()
        );
    }
}
