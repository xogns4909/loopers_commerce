package com.loopers.processor;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import com.loopers.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProcessor {
    
    private final List<EventHandler> eventHandlers;
    private final ProcessedEventService processedEventService;
    
    public void process(GeneralEnvelopeEvent envelope) {
        String messageId = envelope.messageId();
        String eventType = envelope.type();
        
        // 멱등성 체크: 이미 처리된 이벤트인지 확인
        if (!processedEventService.markAsProcessed(messageId, eventType, envelope.correlationId())) {
            log.info("Duplicate event detected, skipping - messageId: {}", messageId);
            return;
        }
        
        log.info("Processing event - type: {}, messageId: {}, payload: {}", 
                eventType, messageId, envelope.payload());
        
        // 비동기로 여러 핸들러 동시 실행 (성능 개선)
        List<CompletableFuture<Void>> futures = eventHandlers.stream()
            .filter(handler -> handler.canHandle(eventType))
            .map(handler -> CompletableFuture.runAsync(() -> {
                try {
                    handler.handle(envelope);
                    log.debug("Handler {} processed event {}", 
                        handler.getClass().getSimpleName(), messageId);
                } catch (Exception e) {
                    // 핸들러 실패는 격리 (다른 핸들러에 영향 없음)
                    log.error("Handler {} failed for event {} - {}", 
                        handler.getClass().getSimpleName(), messageId, e.getMessage());
                }
            }))
            .toList();
        
        // 모든 핸들러 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        if (futures.isEmpty()) {
            log.warn("No handler found for event type: {}", eventType);
        }
    }
}
