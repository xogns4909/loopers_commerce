package com.loopers.infrastructure.cache.keygen;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.application.product.VersionClock;
import com.loopers.infrastructure.cache.util.KeyBuilder;
import java.util.Map;

public abstract class CacheKeyGenerator {
    protected final VersionClock versionClock;

    protected CacheKeyGenerator(VersionClock versionClock) {
        this.versionClock = versionClock;
    }

    protected abstract String getNamespace();
    protected abstract String getPrefix();

    protected CacheKey buildKey(Map<String, Object> params) {
        long version = Long.parseLong(versionClock.current(getNamespace()));
        String keyString = KeyBuilder.build(getPrefix(), getNamespace(), version, params);
        return () -> keyString;
    }
}
