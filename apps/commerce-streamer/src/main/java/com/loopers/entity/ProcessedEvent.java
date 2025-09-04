package com.loopers.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 처리된 이벤트 엔티티
 * 멱등성 보장을 위해 이미 처리된 이벤트를 기록
 */
@Entity
@Table(name = "processed_events",
    indexes = {
        @Index(name = "idx_event_type_processed", columnList = "event_type, processed_at"),
        @Index(name = "idx_correlation_id", columnList = "correlation_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {
    
    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Builder
    public ProcessedEvent(String messageId, String eventType, String correlationId) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.processedAt = LocalDateTime.now();
    }
}
