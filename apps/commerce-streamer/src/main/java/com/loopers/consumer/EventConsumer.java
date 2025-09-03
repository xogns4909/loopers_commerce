package com.loopers.consumer;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.processor.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventProcessor eventProcessor;

    @KafkaListener(topics = "order-events.v1", groupId = "order-consumer")
    public void handleOrderEvents(GeneralEnvelopeEvent envelope, Acknowledgment ack) {
        log.debug("Processing order event: {}", envelope.messageId());
        eventProcessor.process(envelope);
        ack.acknowledge();
    }

    @KafkaListener(topics = "catalog-events.v1", groupId = "catalog-consumer")
    public void handleCatalogEvents(GeneralEnvelopeEvent envelope, Acknowledgment ack) {
        log.debug("Processing catalog event: {}", envelope.messageId());
        eventProcessor.process(envelope);
        ack.acknowledge();
    }

    @KafkaListener(topics = "notification-events.v1", groupId = "notification-consumer")
    public void handleNotificationEvents(GeneralEnvelopeEvent envelope, Acknowledgment ack) {
        log.debug("Processing notification event: {}", envelope.messageId());
        eventProcessor.process(envelope);
        ack.acknowledge();
    }
}
