package com.loopers.retry;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class RetryPolicy {
    
    @Builder.Default
    private final int maxAttempts = 3;
    
    @Builder.Default
    private final Duration initialDelay = Duration.ofMillis(100);
    
    @Builder.Default
    private final Duration maxDelay = Duration.ofMinutes(5);
    
    @Builder.Default
    private final double backoffMultiplier = 2.0;
    
    @Builder.Default
    private final boolean jitterEnabled = true;
    
    public Duration getDelayForAttempt(int attempt) {
        if (attempt <= 0) {
            return Duration.ZERO;
        }
        
        // 지수적 백오프 계산
        long delayMillis = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1));
        
        // 최대 지연 시간 제한
        delayMillis = Math.min(delayMillis, maxDelay.toMillis());
        
        // 지터 적용 (±20% 랜덤)
        if (jitterEnabled) {
            double jitterFactor = 0.8 + (Math.random() * 0.4); // 0.8 ~ 1.2
            delayMillis = (long) (delayMillis * jitterFactor);
        }
        
        return Duration.ofMillis(delayMillis);
    }
    
    public boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }
    
    public boolean isRetryableException(Exception exception) {
        // 재시도할 수 없는 예외들 (비즈니스 로직 오류, 직렬화 오류 등)
        if (exception instanceof IllegalArgumentException ||
            exception instanceof com.fasterxml.jackson.core.JsonProcessingException ||
            exception instanceof ClassCastException) {
            return false;
        }
        
        // 일시적 오류들은 재시도 가능
        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        if (message.contains("timeout") || 
            message.contains("connection") || 
            message.contains("unavailable")) {
            return true;
        }
        
        // 기본적으로 RuntimeException은 재시도
        return exception instanceof RuntimeException;
    }
    
    // 사전 정의된 재시도 정책들
    public static final RetryPolicy FAST = RetryPolicy.builder()
            .maxAttempts(2)
            .initialDelay(Duration.ofMillis(50))
            .maxDelay(Duration.ofSeconds(5))
            .backoffMultiplier(1.5)
            .build();
            
    public static final RetryPolicy STANDARD = RetryPolicy.builder()
            .maxAttempts(3)
            .initialDelay(Duration.ofMillis(100))
            .maxDelay(Duration.ofMinutes(1))
            .backoffMultiplier(2.0)
            .build();
            
    public static final RetryPolicy AGGRESSIVE = RetryPolicy.builder()
            .maxAttempts(5)
            .initialDelay(Duration.ofMillis(200))
            .maxDelay(Duration.ofMinutes(5))
            .backoffMultiplier(2.0)
            .build();
}
