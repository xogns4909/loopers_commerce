package com.loopers.processor;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import com.loopers.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 이벤트 처리 프로세서
 * 
 * 설계 근거:
 * - 멱등성 보장: ProcessedEventService로 중복 처리 방지
 * - 핸들러 패턴: 이벤트 타입별 처리 로직 분리
 * - 확장성: 새로운 이벤트 타입 추가 시 핸들러만 추가
 * - 실패 시 재처리: 예외 발생 시 ACK 하지 않아 재처리 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventProcessor {

    private final List<EventHandler> handlers;
    private final ProcessedEventService processedEventService;

    /**
     * 이벤트 처리
     * 멱등성을 보장하면서 적절한 핸들러로 라우팅
     */
    public void process(GeneralEnvelopeEvent envelope) {
        String messageId = envelope.messageId();
        String eventType = envelope.type();
        String correlationId = envelope.correlationId();
        
        log.debug("Processing event - messageId: {}, type: {}, correlationId: {}", 
                 messageId, eventType, correlationId);
        
        // 멱등성 체크 및 처리
        boolean processed = processedEventService.processIfNotExists(
            messageId, eventType, correlationId,
            () -> handleEvent(envelope)
        );
        
        if (!processed) {
            log.info("Event already processed, skipping - messageId: {}, type: {}", 
                    messageId, eventType);
            // 이미 처리된 이벤트는 정상 처리로 간주하여 ACK
            return;
        }
        
        log.info("Event processing completed - messageId: {}, type: {}", 
                messageId, eventType);
    }

    /**
     * 실제 이벤트 처리
     * 적절한 핸들러 찾아서 처리
     */
    private void handleEvent(GeneralEnvelopeEvent envelope) {
        String eventType = envelope.type();
        boolean handled = false;
        
        // 여러 핸들러가 동일 이벤트를 처리할 수 있음
        for (EventHandler handler : handlers) {
            if (handler.canHandle(eventType)) {
                try {
                    handler.handle(envelope);
                    handled = true;
                    log.debug("Event handled by {} - type: {}", 
                             handler.getClass().getSimpleName(), eventType);
                } catch (Exception e) {
                    log.error("Handler {} failed to process event type: {}", 
                             handler.getClass().getSimpleName(), eventType, e);
                    // 하나의 핸들러라도 실패하면 전체 실패로 처리
                    throw new RuntimeException("Event handling failed", e);
                }
            }
        }
        
        if (!handled) {
            log.warn("No handler found for event type: {}", eventType);
            // 핸들러가 없는 이벤트는 무시하고 ACK
            // 향후 새로운 이벤트 타입이 추가되어도 기존 컨슈머가 멈추지 않음
        }
    }
}
