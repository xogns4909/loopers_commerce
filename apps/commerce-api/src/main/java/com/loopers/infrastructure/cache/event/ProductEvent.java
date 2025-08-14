package com.loopers.infrastructure.cache.event;

import com.loopers.infrastructure.cache.strategy.UpdateEvent;
import com.loopers.infrastructure.cache.strategy.UpdateType;

public record ProductEvent(Long id, UpdateType type) implements UpdateEvent { }
