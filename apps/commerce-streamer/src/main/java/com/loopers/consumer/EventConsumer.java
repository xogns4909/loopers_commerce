package com.loopers.consumer;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.impl.MetricsHandler;
import com.loopers.retry.RetryPolicy;
import com.loopers.retry.RetryableEventProcessor;
import java.util.List;
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

    private final RetryableEventProcessor retryableEventProcessor;
    private final MetricsHandler metricsHandler;

    @KafkaListener(topics = "order-events.v1", groupId = "order-consumer")
    public void handleOrderEvents(GeneralEnvelopeEvent envelope, 
                                 Acknowledgment ack,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Order event received - messageId: {}, type: {}, offset: {}:{}", 
                envelope.messageId(), envelope.type(), partition, offset);
        
        try {
            // 주문 이벤트는 중요하므로 적극적인 재시도 정책 적용
            retryableEventProcessor.processWithRetry(
                envelope, topic, partition, offset, "order-consumer", RetryPolicy.AGGRESSIVE);
            
            ack.acknowledge();
            log.debug("Order event acknowledged - messageId: {}", envelope.messageId());
            
        } catch (Exception e) {
            log.error("Critical: Failed to process order event after all retries - messageId: {}. " +
                     "Event has been sent to Dead Letter Table.", envelope.messageId(), e);
            
            // 재시도 로직에서 이미 DLT로 보냈으므로 ACK 처리
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "catalog-events.v1", groupId = "catalog-consumer")
    public void handleCatalogEvents(List<GeneralEnvelopeEvent> envelopes,
                                   Acknowledgment ack,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Catalog events batch received - count: {}, topic: {}, partition: {}", 
                envelopes.size(), topic, partition);

        for (GeneralEnvelopeEvent envelope : envelopes) {
            retryableEventProcessor.processWithRetry(
                envelope, topic, partition, offset, "catalog-consumer", RetryPolicy.STANDARD);
        }

        metricsHandler.handleBatch(envelopes);
        
        ack.acknowledge();
        log.debug("Catalog events batch processed successfully - count: {}", envelopes.size());
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
            // 알림 이벤트는 빠른 재시도 정책 적용 (덜 중요하므로)
            retryableEventProcessor.processWithRetry(
                envelope, topic, partition, offset, "notification-consumer", RetryPolicy.FAST);
            
            ack.acknowledge();
            log.debug("Notification event acknowledged - messageId: {}", envelope.messageId());
            
        } catch (Exception e) {
            log.error("Critical: Failed to process notification event after all retries - messageId: {}. " +
                     "Event has been sent to Dead Letter Table.", envelope.messageId(), e);
            
            // 재시도 로직에서 이미 DLT로 보냈으므로 ACK 처리
            ack.acknowledge();
        }
    }
}
