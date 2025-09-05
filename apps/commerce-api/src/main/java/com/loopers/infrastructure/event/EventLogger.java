package com.loopers.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class EventLogger {

    @Async("eventLoggingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void logAll(Envelope<?> envelope) {
        log.info("Event Published | type: {} | messageId: {} | source: {} | correlationId: {} | occurredAt: {} | payload: {}",
            envelope.type(),
            envelope.messageId(),
            envelope.source(),
            envelope.correlationId(),
            envelope.occurredAt(),
            envelope.payload().getClass().getSimpleName()
        );
    }
}
