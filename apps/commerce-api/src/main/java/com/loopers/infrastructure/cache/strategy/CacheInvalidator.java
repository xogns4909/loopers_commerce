package com.loopers.infrastructure.cache.strategy;

import com.loopers.infrastructure.cache.strategy.UpdateEvent;


public interface CacheInvalidator<E extends UpdateEvent> {
    

    void on(E event);
}
