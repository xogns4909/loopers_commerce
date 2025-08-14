package com.loopers.application.cache;


import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductCachePolicy implements CachePolicy {

    @Override
    public Duration ttlDetail() {
        return Duration.ofMinutes(20);
    }

    @Override
    public Duration ttlList() {
        return Duration.ofSeconds(90);
    }

    @Override
    public Duration ttlNull() {
        return Duration.ofSeconds(30);
    }

    @Override
    public Duration withJitter(Duration base) {
        long ms = base.toMillis();
        long delta = ThreadLocalRandom.current().nextLong(ms / 5 + 1) - (ms / 10); // -10%~+10%
        return Duration.ofMillis(Math.max(1000, ms + delta));
    }
}
