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
    private final KafkaTemplate<String, GeneralEnvelopeEvent> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void relayEvents() {
        List<OutboxEvent> newEvents = outboxEventRepository
            .findTop100ByStatusOrderByCreatedAt(OutboxEvent.OutboxStatus.NEW);

        if (newEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : newEvents) {
            if (claimAndSendSingle(event)) {
                log.debug("Event sent successfully: {}", event.getMessageId());
            }
        }
    }

    @Transactional
    protected boolean claimAndSendSingle(OutboxEvent event) {
        int claimedCount = outboxEventRepository.claimEventsForSending(List.of(event.getId()));
        if (claimedCount == 0) {
            return false;
        }

        event.markAsSending();

        try {

            SendResult<String, GeneralEnvelopeEvent> result = kafkaTemplate
                .send(event.getTopic(), event.getEventKey(), event.toGeneralEnvelopeEvent())
                .get();

            event.markAsPublished();
            outboxEventRepository.save(event);
            return true;

        } catch (InterruptedException | ExecutionException e) {

            log.error("Failed to send event: {}", event.getMessageId(), e);
            event.markAsFailed();
            outboxEventRepository.save(event);
            return false;
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
