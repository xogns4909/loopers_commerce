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
        String messageId   = envelope.messageId();
        String eventType   = envelope.type();
        String correlation = envelope.correlationId();


        boolean acquired = processedEventService.tryStart(messageId, eventType, correlation);
        if (!acquired) {
            log.info("Duplicate or already processing/processed, skip - messageId: {}", messageId);
            return;
        }

        log.info("Processing event - type: {}, messageId: {}, payload: {}",
            eventType, messageId, envelope.payload());

        // 디버깅: 등록된 핸들러들 확인
        log.info("Registered handlers: {}", eventHandlers.size());
        for (EventHandler handler : eventHandlers) {
            boolean canHandle = handler.canHandle(eventType);
            log.info("Handler: {} - canHandle({}): {}", 
                    handler.getClass().getSimpleName(), eventType, canHandle);
        }


        List<CompletableFuture<Void>> futures = eventHandlers.stream()
            .filter(h -> h.canHandle(eventType))
            .map(h -> CompletableFuture.runAsync(() -> h.handle(envelope)))
            .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            if (futures.isEmpty()) {
                log.warn("No handler found for event type: {}", eventType);
            }


            processedEventService.markProcessed(messageId);
            log.debug("Event processed successfully - messageId: {}", messageId);

        } catch (Exception ex) {

            processedEventService.markFailed(messageId);
            log.error("Event processing failed - messageId: {}, err={}", messageId, ex.getMessage(), ex);
            // 예외는 컨슈머 레이어에서 DLQ 정책이 처리 (여기선 삼켜도 되고 던져도 됨)
            throw ex;
        }
    }
}
