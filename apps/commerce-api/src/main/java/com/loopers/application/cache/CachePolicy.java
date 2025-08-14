package com.loopers.application.cache;

import java.time.Duration;

public interface CachePolicy {

    Duration ttlDetail();

    Duration ttlList();

    Duration ttlNull();

    Duration withJitter(Duration base);
}
