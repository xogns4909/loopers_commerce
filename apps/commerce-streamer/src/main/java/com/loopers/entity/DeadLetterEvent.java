package com.loopers.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "dead_letter_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeadLetterEvent extends BaseEntity {

    @Column(name = "message_id", nullable = false, length = 128, unique = true)
    private String messageId;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "partition_id", nullable = false)
    private Integer partitionId;

    @Column(name = "offset_id", nullable = false)
    private Long offsetId;

    @Column(name = "consumer_group", nullable = false, length = 100)
    private String consumerGroup;

    @Column(name = "payload", columnDefinition = "JSON", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", nullable = false)
    private FailureReason failureReason;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "retry_attempts", nullable = false)
    private Integer retryAttempts = 0;

    @Column(name = "first_failed_at", nullable = false)
    private ZonedDateTime firstFailedAt;

    @Column(name = "last_failed_at", nullable = false)
    private ZonedDateTime lastFailedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DEAD;

    @Column(name = "resolved_at")
    private ZonedDateTime resolvedAt;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    public enum FailureReason {
        DESERIALIZATION_ERROR,
        HANDLER_EXCEPTION,
        TIMEOUT,
        RESOURCE_UNAVAILABLE,
        VALIDATION_ERROR,
        UNKNOWN_ERROR
    }

    public enum Status {
        DEAD,           // DLT에 저장됨
        INVESTIGATING,  // 조사 중
        REPROCESSING,   // 재처리 중
        RESOLVED        // 해결됨
    }

    private DeadLetterEvent(String messageId, String originalTopic, String eventType, String correlationId,
                           Integer partitionId, Long offsetId, String consumerGroup, String payload,
                           FailureReason failureReason, String errorMessage, String stackTrace, Integer retryAttempts) {
        this.messageId = messageId;
        this.originalTopic = originalTopic;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.partitionId = partitionId;
        this.offsetId = offsetId;
        this.consumerGroup = consumerGroup;
        this.payload = payload;
        this.failureReason = failureReason;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.retryAttempts = retryAttempts;
        this.firstFailedAt = ZonedDateTime.now();
        this.lastFailedAt = ZonedDateTime.now();
    }

    public static DeadLetterEvent create(String messageId, String originalTopic, String eventType, String correlationId,
                                       Integer partitionId, Long offsetId, String consumerGroup, String payload,
                                       FailureReason failureReason, String errorMessage, String stackTrace, Integer retryAttempts) {
        return new DeadLetterEvent(messageId, originalTopic, eventType, correlationId, partitionId, offsetId,
                                 consumerGroup, payload, failureReason, errorMessage, stackTrace, retryAttempts);
    }

    public void markAsInvestigating(String investigator) {
        this.status = Status.INVESTIGATING;
        this.resolvedBy = investigator;
    }

    public void markAsReprocessing() {
        this.status = Status.REPROCESSING;
    }

    public void markAsResolved(String resolvedBy, String notes) {
        this.status = Status.RESOLVED;
        this.resolvedAt = ZonedDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = notes;
    }
}
