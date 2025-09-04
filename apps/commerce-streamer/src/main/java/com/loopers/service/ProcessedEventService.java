package com.loopers.service;

import com.loopers.entity.ProcessedEvent;
import com.loopers.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedEventService {
    
    private final ProcessedEventRepository processedEventRepository;
    

    @Transactional
    public boolean markAsProcessed(String messageId, String eventType, String correlationId) {
        try {
            if (processedEventRepository.existsById(messageId)) {
                log.warn("Event already processed - messageId: {}, type: {}", messageId, eventType);
                return false;
            }

            ProcessedEvent processedEvent = ProcessedEvent.builder()
                .messageId(messageId)
                .eventType(eventType)
                .correlationId(correlationId)
                .build();
                
            processedEventRepository.save(processedEvent);
            return true;
            
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event processing attempted - messageId: {}", messageId);
            return false;
        }
    }
}
