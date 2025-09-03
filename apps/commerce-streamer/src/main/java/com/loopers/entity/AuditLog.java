package com.loopers.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 감사 로그 엔티티
 * 봉투 패턴(GeneralEnvelopeEvent) 그대로 저장
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {
    
    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;
    
    @Column(name = "type", nullable = false, length = 100)
    private String type;
    
    @Column(name = "schema_version", length = 10)
    private String schemaVersion;
    
    @Column(name = "occurred_at", nullable = false, length = 30)
    private String occurredAt;
    
    @Column(name = "source", length = 100)
    private String source;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;  // JSON 문자열로 저장
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public AuditLog(String messageId, String type, String schemaVersion, 
                   String occurredAt, String source, String correlationId, String payload) {
        this.messageId = messageId;
        this.type = type;
        this.schemaVersion = schemaVersion;
        this.occurredAt = occurredAt;
        this.source = source;
        this.correlationId = correlationId;
        this.payload = payload;
    }
}
