package com.loopers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.entity.AuditLog;
import com.loopers.repository.AuditLogRepository;
import com.loopers.event.GeneralEnvelopeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload - messageId: {}", envelope.messageId(), e);
            // payload 없이라도 저장 시도
            saveWithoutPayload(envelope);
        } catch (DataAccessException e) {
            log.error("Database error while saving audit log - messageId: {}", envelope.messageId(), e);
            throw e;
        }
    }
    
    private void saveWithoutPayload(GeneralEnvelopeEvent envelope) {
        AuditLog auditLog = AuditLog.builder()
            .messageId(envelope.messageId())
            .type(envelope.type())
            .schemaVersion(envelope.schemaVersion())
            .occurredAt(envelope.occurredAt())
            .source(envelope.source())
            .correlationId(envelope.correlationId())
            .payload("{\"error\":\"Failed to serialize original payload\"}")
            .build();
        
        auditLogRepository.save(auditLog);
        log.warn("Audit log saved without original payload - messageId: {}", envelope.messageId());
    }
}
