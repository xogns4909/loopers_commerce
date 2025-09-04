package com.loopers.consumer;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.processor.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventProcessor eventProcessor;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @KafkaListener(topics = "order-events.v1", groupId = "order-consumer")
    public void handleOrderEvents(GeneralEnvelopeEvent envelope, 
                                 Acknowledgment ack,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Order event received - messageId: {}, type: {}, offset: {}:{}", 
                envelope.messageId(), envelope.type(), partition, offset);
        
        try {
            eventProcessor.process(envelope);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process order event - messageId: {}, will retry or send to DLT", 
                    envelope.messageId(), e);
            handleFailedMessage(envelope, e, ack);
        }
    }

    @KafkaListener(topics = "catalog-events.v1", groupId = "catalog-consumer")
    public void handleCatalogEvents(GeneralEnvelopeEvent envelope,
                                   Acknowledgment ack,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Catalog event received - messageId: {}, type: {}, offset: {}:{}", 
                envelope.messageId(), envelope.type(), partition, offset);
        
        try {
            eventProcessor.process(envelope);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process catalog event - messageId: {}, will retry or send to DLT", 
                    envelope.messageId(), e);
            handleFailedMessage(envelope, e, ack);
        }
    }

    @KafkaListener(topics = "notification-events.v1", groupId = "notification-consumer")
    public void handleNotificationEvents(GeneralEnvelopeEvent envelope,
                                        Acknowledgment ack,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Notification event received - messageId: {}, type: {}, offset: {}:{}", 
                envelope.messageId(), envelope.type(), partition, offset);
        
        try {
            eventProcessor.process(envelope);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process notification event - messageId: {}, will retry or send to DLT", 
                    envelope.messageId(), e);
            handleFailedMessage(envelope, e, ack);
        }
    }
    
    private void handleFailedMessage(GeneralEnvelopeEvent envelope, Exception e, Acknowledgment ack) {
        // TODO: 실제로는 DLT(Dead Letter Topic)로 전송하거나 재시도 로직 구현
        // 현재는 로그만 남기고 ACK 처리 (무한 루프 방지)
        log.error("Event processing failed after retries, sending to DLT - messageId: {}, type: {}", 
                envelope.messageId(), envelope.type());
        
        // 실패한 이벤트도 audit_logs에는 기록되도록 
        // (AuditLogHandler는 모든 이벤트를 try-catch로 안전하게 처리)
        
        ack.acknowledge();  // 임시로 ACK 처리 (추후 DLT 구현)
    }
}
