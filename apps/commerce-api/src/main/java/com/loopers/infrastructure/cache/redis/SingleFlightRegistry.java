package com.loopers.infrastructure.cache.redis;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;


public class SingleFlightRegistry {
    
    private final ConcurrentMap<String, CompletableFuture<String>> inflight = new ConcurrentHashMap<>();
    
    public CompletableFuture<String> computeIfAbsent(String key, Supplier<CompletableFuture<String>> supplier) {
        return inflight.computeIfAbsent(key, k -> supplier.get())
                      .whenComplete((result, throwable) -> inflight.remove(key));
    }
    
    public int getInflightCount() {
        return inflight.size();
    }
}
