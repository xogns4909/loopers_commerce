package com.loopers.infrastructure.outbox;

import com.loopers.domain.BaseEntity;
import com.loopers.infrastructure.event.GeneralEnvelopeEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion = "v1";

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "payload", columnDefinition = "JSON", nullable = false)
    @Convert(converter = PayloadJsonConverter.class)
    private Object payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_retry_at")
    private ZonedDateTime nextRetryAt;

    public static OutboxEvent create(String topic, String eventKey, GeneralEnvelopeEvent<?> envelope) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.topic = topic;
        outboxEvent.eventKey = eventKey;
        outboxEvent.messageId = envelope.messageId();
        outboxEvent.eventType = envelope.type();
        outboxEvent.schemaVersion = envelope.schemaVersion();
        outboxEvent.source = envelope.source();
        outboxEvent.correlationId = envelope.correlationId();
        outboxEvent.payload = envelope.payload();
        return outboxEvent;
    }

    public GeneralEnvelopeEvent<?> toGeneralEnvelopeEvent() {
        return new GeneralEnvelopeEvent<>(
            messageId,
            eventType,
            schemaVersion,
            getCreatedAt().toString(),
            source,
            correlationId,
            payload
        );
    }

    public void markAsSending() {
        this.status = OutboxStatus.SENDING;
        this.nextRetryAt = null;
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.nextRetryAt = null;
    }

    /** 실패 시 재시도 스케줄링(백오프) */
    public void markAsFailed(String error, RetryPolicy policy) {
        this.status = OutboxStatus.FAILED;
        this.retryCount = this.retryCount + 1;
        this.lastError = error;
        long backoffMs = policy.backoffMillis(this.retryCount);
        this.nextRetryAt = ZonedDateTime.now().plusNanos(backoffMs * 1_000_000);
    }

    public enum OutboxStatus {
        NEW, SENDING, PUBLISHED, FAILED
    }
}
