package com.loopers.infrastructure.event;

import com.loopers.infrastructure.outbox.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DomainEventBridge {

    private final ApplicationEventPublisher publisher;
    private final EnvelopeFactory envelopeFactory;
    private final OutboxEventService outboxEventService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishEvent(EventType eventType, Object payload) {
        Envelope<Object> envelope = envelopeFactory.create(eventType, payload);

        publisher.publishEvent(envelope);

        outboxEventService.save(envelope);
    }

}
