package com.loopers.infrastructure.cache.core;

import java.time.Duration;

public interface CachePolicy {

    Duration ttlDetail();

    Duration ttlList();

    Duration ttlNull();

    Duration withJitter(Duration base);
}
