package com.loopers.application.cache;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.function.Supplier;

@Component
public class ReadThroughCache {
    public <V> V getOrLoad(CacheStore<V> store, CacheKey key,
        Duration ttl, Supplier<V> loader, Duration nullTtl) {
        return store.get(key).orElseGet(() -> {
            V fresh = loader.get();
            store.put(key, fresh, fresh == null ? nullTtl : ttl);
            return fresh;
        });
    }
}
