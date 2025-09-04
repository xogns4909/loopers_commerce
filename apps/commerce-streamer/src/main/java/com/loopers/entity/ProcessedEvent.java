package com.loopers.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    @Column(name = "message_id", length = 128)
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PROCESSING; // 기본은 선점 중

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt = ZonedDateTime.now();

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 1;

    public enum Status { PROCESSING, PROCESSED, FAILED }

    private ProcessedEvent(String messageId, String eventType, String correlationId) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.correlationId = correlationId;
    }

    public static ProcessedEvent start(String messageId, String eventType, String correlationId) {
        return new ProcessedEvent(messageId, eventType, correlationId);
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
        this.processedAt = ZonedDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.processedAt = ZonedDateTime.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}
