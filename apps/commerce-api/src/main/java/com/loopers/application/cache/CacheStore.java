package com.loopers.application.cache;


import java.time.Duration;
import java.util.Optional;

public interface CacheStore<V> {

    Optional<V> get(CacheKey key);

    void put(CacheKey key, V value, Duration ttl);

    void evict(CacheKey key);
}
