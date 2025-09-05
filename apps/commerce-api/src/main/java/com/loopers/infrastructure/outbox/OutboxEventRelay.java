package com.loopers.infrastructure.outbox;

import com.loopers.infrastructure.event.GeneralEnvelopeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private final RetryPolicy retryPolicy = new DefaultRetryPolicy();

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void relayEvents() {
        List<OutboxEvent> ready = outboxEventRepository.findTop100ReadyToSend(ZonedDateTime.now());
        if (ready.isEmpty()) return;

        int claimed = outboxEventRepository.claimEventsForSending(
            ready.stream().map(OutboxEvent::getId).toList(),
            ZonedDateTime.now() 
        );
        if (claimed == 0) return;

        for (OutboxEvent event : ready) {
            try {
                sendOnce(event);
            } catch (Exception ex) {
                log.error("Unexpected error while sending event: {}", event.getMessageId(), ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    protected void sendOnce(OutboxEvent event) {
        event.markAsSending();

        try {
            SendResult<String, Object> result = (SendResult<String, Object>) kafkaTemplate
                .send(event.getTopic(), event.getEventKey(), event.toGeneralEnvelopeEvent()).get();

            event.markAsPublished();
            outboxEventRepository.save(event);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // 인터럽트 복구
            log.error("Interrupted while sending event: {}", event.getMessageId(), ie);
            event.markAsFailed(ie.getMessage(), retryPolicy);
            outboxEventRepository.save(event);

        } catch (ExecutionException ee) {
            log.error("Failed to send event: {}", event.getMessageId(), ee);
            event.markAsFailed(ee.getMessage(), retryPolicy);
            outboxEventRepository.save(event);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldEvents() {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(7);
        int deletedCount = outboxEventRepository.softDeleteOldPublishedEvents(cutoffDate);
        if (deletedCount > 0) {
            log.info("Cleaned up {} old published events", deletedCount);
        }
    }
}
