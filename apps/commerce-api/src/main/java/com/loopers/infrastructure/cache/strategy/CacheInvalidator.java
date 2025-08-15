package com.loopers.infrastructure.cache.strategy;

import com.loopers.infrastructure.cache.strategy.UpdateEvent;

/**
 * 캐시 무효화 전략 인터페이스
 * 이벤트 기반으로 캐시를 무효화
 */
public interface CacheInvalidator<E extends UpdateEvent> {
    
    /**
     * 도메인 이벤트를 받아 적절한 캐시 무효화 수행
     */
    void on(E event);
}
