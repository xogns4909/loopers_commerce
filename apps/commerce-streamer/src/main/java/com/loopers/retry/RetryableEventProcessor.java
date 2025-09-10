package com.loopers.retry;

import com.loopers.entity.DeadLetterEvent;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.processor.EventProcessor;
import com.loopers.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableEventProcessor {

    private final EventProcessor eventProcessor;
    private final DeadLetterService deadLetterService;

    /**
     * 표준 재시도 정책으로 이벤트 처리
     */
    public void processWithRetry(GeneralEnvelopeEvent envelope, String originalTopic, 
                               Integer partitionId, Long offsetId, String consumerGroup) {
        processWithRetry(envelope, originalTopic, partitionId, offsetId, consumerGroup, RetryPolicy.STANDARD);
    }


    public void processWithRetry(GeneralEnvelopeEvent envelope, String originalTopic, 
                               Integer partitionId, Long offsetId, String consumerGroup, 
                               RetryPolicy retryPolicy) {
        
        AtomicInteger attemptCount = new AtomicInteger(1);
        Exception lastException = null;

        while (attemptCount.get() <= retryPolicy.getMaxAttempts()) {
            try {
                log.debug("Processing event attempt {} - messageId: {}", 
                         attemptCount.get(), envelope.messageId());
                
                eventProcessor.process(envelope);
                
                log.debug("Event processed successfully on attempt {} - messageId: {}", 
                         attemptCount.get(), envelope.messageId());
                return; // 성공
                
            } catch (Exception e) {
                lastException = e;
                handleProcessingFailure(envelope, originalTopic, partitionId, offsetId, 
                                      consumerGroup, e, attemptCount, retryPolicy);
                

                if (shouldTerminateRetry(e, attemptCount.get(), retryPolicy)) {
                    return;
                }
                
                waitBeforeRetry(retryPolicy.getDelayForAttempt(attemptCount.get()), 
                              envelope.messageId(), attemptCount.get());
                attemptCount.incrementAndGet();
            }
        }
        

        log.error("All retry attempts failed - messageId: {}", envelope.messageId(), lastException);
        sendToDeadLetter(envelope, originalTopic, partitionId, offsetId, consumerGroup, 
                        lastException, attemptCount.get() - 1);
    }

    private void handleProcessingFailure(GeneralEnvelopeEvent envelope, String originalTopic,
                                       Integer partitionId, Long offsetId, String consumerGroup,
                                       Exception e, AtomicInteger attemptCount, RetryPolicy retryPolicy) {
        
        log.warn("Event processing failed on attempt {}/{} - messageId: {}, error: {}",
                attemptCount.get(), retryPolicy.getMaxAttempts(), envelope.messageId(), e.getMessage());


        if (!retryPolicy.isRetryableException(e)) {
            log.error("Non-retryable exception - messageId: {}, sending to DLT immediately",
                     envelope.messageId(), e);
            sendToDeadLetter(envelope, originalTopic, partitionId, offsetId, consumerGroup, 
                           e, attemptCount.get());
            return;
        }


        if (!retryPolicy.shouldRetry(attemptCount.get())) {
            log.error("Max retry attempts exceeded - messageId: {}, sending to DLT",
                     envelope.messageId(), e);
            sendToDeadLetter(envelope, originalTopic, partitionId, offsetId, consumerGroup, 
                           e, attemptCount.get());
        }
    }

    private boolean shouldTerminateRetry(Exception e, int currentAttempt, RetryPolicy retryPolicy) {
        return !retryPolicy.isRetryableException(e) || 
               !retryPolicy.shouldRetry(currentAttempt) ||
               Thread.currentThread().isInterrupted();
    }

    private void waitBeforeRetry(Duration delay, String messageId, int currentAttempt) {
        try {
            log.debug("Waiting {}ms before retry attempt {} - messageId: {}",
                     delay.toMillis(), currentAttempt + 1, messageId);
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Retry interrupted - messageId: {}", messageId, ie);
            throw new RuntimeException("Retry interrupted", ie);
        }
    }

    private void sendToDeadLetter(GeneralEnvelopeEvent envelope, String originalTopic, 
                                Integer partitionId, Long offsetId, String consumerGroup,
                                Exception exception, Integer retryAttempts) {
        try {
            deadLetterService.sendToDeadLetter(envelope, originalTopic, partitionId, offsetId, 
                                             consumerGroup, exception, retryAttempts);
            
            log.info("Event sent to DLT - messageId: {}, reason: {}, attempts: {}", 
                    envelope.messageId(), determineFailureReason(exception), retryAttempts);
            
        } catch (Exception dltException) {
            log.error("CRITICAL: Failed to send event to DLT - messageId: {}. " +
                     "Original: {}, DLT error: {}",
                     envelope.messageId(), exception.getMessage(), dltException.getMessage(), 
                     dltException);
        }
    }

    private DeadLetterEvent.FailureReason determineFailureReason(Exception exception) {
        String className = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        
        // JSON/직렬화 오류
        if (className.contains("JsonProcessing") || className.contains("Deserialization")) {
            return DeadLetterEvent.FailureReason.DESERIALIZATION_ERROR;
        }
        
        // 타임아웃 오류
        if (className.contains("Timeout") || message.contains("timeout")) {
            return DeadLetterEvent.FailureReason.TIMEOUT;
        }
        
        // 검증 오류
        if (className.contains("Validation") || message.contains("validation") || message.contains("invalid")) {
            return DeadLetterEvent.FailureReason.VALIDATION_ERROR;
        }
        
        // 리소스 사용불가
        if (message.contains("connection") || message.contains("unavailable") || message.contains("resource")) {
            return DeadLetterEvent.FailureReason.RESOURCE_UNAVAILABLE;
        }
        
        // 핸들러 예외
        if (exception instanceof RuntimeException) {
            return DeadLetterEvent.FailureReason.HANDLER_EXCEPTION;
        }
        
        return DeadLetterEvent.FailureReason.UNKNOWN_ERROR;
    }
}
