package com.loopers.infrastructure.cache.strategy;


public interface UpdateEvent {
    UpdateType type();
    Long id();
}
