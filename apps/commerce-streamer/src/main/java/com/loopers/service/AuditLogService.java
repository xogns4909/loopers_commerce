package com.loopers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.entity.AuditLog;
import com.loopers.repository.AuditLogRepository;
import com.loopers.event.GeneralEnvelopeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void saveAuditLog(GeneralEnvelopeEvent envelope) {
        try {
            // payload를 JSON 문자열로 변환
            String payloadJson = objectMapper.writeValueAsString(envelope.payload());
            
            AuditLog auditLog = AuditLog.builder()
                .messageId(envelope.messageId())
                .type(envelope.type())
                .schemaVersion(envelope.schemaVersion())
                .occurredAt(envelope.occurredAt())
                .source(envelope.source())
                .correlationId(envelope.correlationId())
                .payload(payloadJson)
                .build();
            
            auditLogRepository.save(auditLog);
            
            log.info("Audit log saved - messageId: {}, type: {}", 
                    envelope.messageId(), envelope.type());
                    
        } catch (Exception e) {
            log.error("Failed to save audit log - messageId: {}", envelope.messageId(), e);
            throw new RuntimeException("Failed to save audit log", e);
        }
    }
}
