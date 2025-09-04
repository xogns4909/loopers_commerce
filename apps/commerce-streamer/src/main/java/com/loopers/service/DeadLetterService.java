package com.loopers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.entity.DeadLetterEvent;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.repository.DeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterService {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void sendToDeadLetter(GeneralEnvelopeEvent envelope, String originalTopic, 
                               Integer partitionId, Long offsetId, String consumerGroup,
                               Exception exception, Integer retryAttempts) {
        
        try {
            String payload = objectMapper.writeValueAsString(envelope);
            
            DeadLetterEvent.FailureReason failureReason = determineFailureReason(exception);
            String errorMessage = exception.getMessage();
            String stackTrace = getStackTraceAsString(exception);
            
            DeadLetterEvent deadLetterEvent = DeadLetterEvent.create(
                envelope.messageId(),
                originalTopic,
                envelope.type(),
                envelope.correlationId(),
                partitionId,
                offsetId,
                consumerGroup,
                payload,
                failureReason,
                errorMessage,
                stackTrace,
                retryAttempts
            );
            
            deadLetterEventRepository.save(deadLetterEvent);
            
            log.error("Event sent to Dead Letter Table - messageId: {}, eventType: {}, reason: {}, retryAttempts: {}",
                    envelope.messageId(), envelope.type(), failureReason, retryAttempts);
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize envelope for DLT - messageId: {}", envelope.messageId(), e);
            
            // JSON 직렬화 실패해도 DLT에는 저장해야 함
            DeadLetterEvent deadLetterEvent = DeadLetterEvent.create(
                envelope.messageId(),
                originalTopic,
                envelope.type(),
                envelope.correlationId(),
                partitionId,
                offsetId,
                consumerGroup,
                "SERIALIZATION_FAILED: " + envelope.toString(),
                DeadLetterEvent.FailureReason.DESERIALIZATION_ERROR,
                "Failed to serialize envelope: " + e.getMessage(),
                getStackTraceAsString(e),
                retryAttempts
            );
            
            deadLetterEventRepository.save(deadLetterEvent);
        }
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterEvent> getDeadLetterEvents(Pageable pageable) {
        return deadLetterEventRepository.findByStatusOrderByLastFailedAtDesc(
                DeadLetterEvent.Status.DEAD, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterEvent> getDeadLetterEventsByType(String eventType, Pageable pageable) {
        return deadLetterEventRepository.findByEventTypeAndStatusOrderByLastFailedAtDesc(
                eventType, DeadLetterEvent.Status.DEAD, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterEvent> getDeadLetterEventsByFailureReason(DeadLetterEvent.FailureReason failureReason, Pageable pageable) {
        return deadLetterEventRepository.findByFailureReasonAndStatusOrderByLastFailedAtDesc(
                failureReason, DeadLetterEvent.Status.DEAD, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DeadLetterEvent> getDeadLetterEventByMessageId(String messageId) {
        return deadLetterEventRepository.findByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public List<DeadLetterEventRepository.DeadLetterEventSummary> getFailureSummary(int hours) {
        ZonedDateTime since = ZonedDateTime.now().minusHours(hours);
        return deadLetterEventRepository.getFailureSummary(since);
    }

    @Transactional(readOnly = true)
    public long getDeadLetterCount(int hours) {
        ZonedDateTime since = ZonedDateTime.now().minusHours(hours);
        return deadLetterEventRepository.countDeadEventsSince(since);
    }

    @Transactional
    public void markAsInvestigating(Long id, String investigator) {
        Optional<DeadLetterEvent> eventOpt = deadLetterEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            DeadLetterEvent event = eventOpt.get();
            event.markAsInvestigating(investigator);
            deadLetterEventRepository.save(event);
            log.info("DeadLetterEvent marked as investigating - id: {}, investigator: {}", id, investigator);
        }
    }

    @Transactional
    public int bulkMarkAsResolved(List<Long> ids, String resolvedBy, String notes) {
        int updated = deadLetterEventRepository.markAsResolved(ids, ZonedDateTime.now(), resolvedBy, notes);
        log.info("Bulk resolved {} dead letter events by {}", updated, resolvedBy);
        return updated;
    }

    /**
     * 재처리를 위한 이벤트 복원
     */
    @Transactional(readOnly = true)
    public Optional<GeneralEnvelopeEvent> reconstructEvent(String messageId) {
        return deadLetterEventRepository.findByMessageId(messageId)
                .map(deadLetterEvent -> {
                    try {
                        return objectMapper.readValue(deadLetterEvent.getPayload(), GeneralEnvelopeEvent.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to reconstruct event from DLT - messageId: {}", messageId, e);
                        return null;
                    }
                });
    }

    private DeadLetterEvent.FailureReason determineFailureReason(Exception exception) {
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        
        if (exceptionType.contains("JsonProcessing") || exceptionType.contains("Deserialization")) {
            return DeadLetterEvent.FailureReason.DESERIALIZATION_ERROR;
        }
        
        if (exceptionType.contains("Timeout") || message.contains("timeout")) {
            return DeadLetterEvent.FailureReason.TIMEOUT;
        }
        
        if (exceptionType.contains("Validation") || message.contains("validation") || message.contains("invalid")) {
            return DeadLetterEvent.FailureReason.VALIDATION_ERROR;
        }
        
        if (message.contains("connection") || message.contains("unavailable") || message.contains("resource")) {
            return DeadLetterEvent.FailureReason.RESOURCE_UNAVAILABLE;
        }
        
        if (exception instanceof RuntimeException) {
            return DeadLetterEvent.FailureReason.HANDLER_EXCEPTION;
        }
        
        return DeadLetterEvent.FailureReason.UNKNOWN_ERROR;
    }

    private String getStackTraceAsString(Exception exception) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Failed to get stack trace: " + e.getMessage();
        }
    }
}
