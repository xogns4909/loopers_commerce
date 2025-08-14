package com.loopers.infrastructure.cache.core;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.infrastructure.cache.core.CachePolicy;

public interface CacheService {
    
    <T> T getOrLoad(CacheKey key, TypeRef<T> typeRef, Loader<T> loader, CachePolicy policy);
    
    <T> void preload(CacheKey key, TypeRef<T> typeRef, Loader<T> loader, CachePolicy policy);
    
    void evict(CacheKey key);
    
    void bumpNamespace(String namespace);
    
    @FunctionalInterface
    interface Loader<T> {
        T load() throws Exception;
    }
}
