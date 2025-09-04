package com.loopers.infrastructure.outbox;

import com.loopers.infrastructure.event.Envelope;
import com.loopers.infrastructure.event.EventRoutingStrategy;
import com.loopers.infrastructure.event.EventType;
import com.loopers.infrastructure.event.GeneralEnvelopeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final EventRoutingStrategy eventRoutingStrategy;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Envelope<?> envelope) {
        EventType eventType = EventType.fromString(envelope.type());

        String topic = eventRoutingStrategy.getTopic(eventType);
        String key = eventRoutingStrategy.getKey(envelope.payload());

        GeneralEnvelopeEvent<?> generalEvent = GeneralEnvelopeEvent.from(envelope);

        OutboxEvent outboxEvent = OutboxEvent.create(topic, key, generalEvent);
        outboxEventRepository.save(outboxEvent);
    }

    public boolean isAlreadyExists(String messageId) {
        return outboxEventRepository.existsByMessageId(messageId);
    }

    public long countByStatus(OutboxEvent.OutboxStatus status) {
        return outboxEventRepository.countByStatus(status);
    }
}
