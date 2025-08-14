package com.loopers.infrastructure.cache.redis;

import com.loopers.application.cache.*;
import com.loopers.infrastructure.cache.redis.codec.ValueCodec;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;

public class RedisCacheStore<V> implements CacheStore<V> {
    private final RedisTemplate<String,String> redis;
    private final ValueCodec<V> codec;
    private final CachePolicy policy;

    public RedisCacheStore(RedisTemplate<String,String> redis, ValueCodec<V> codec, CachePolicy policy){
        this.redis=redis; this.codec=codec; this.policy=policy;
    }

    @Override
    public Optional<V> get(CacheKey key) {
        try {
            String raw = redis.opsForValue().get(key.asString());
            return Optional.ofNullable(codec.deserialize(raw));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void put(CacheKey key, V value, Duration ttl) {
        try {
            String json = codec.serialize(value);
            redis.opsForValue().set(key.asString(), json, policy.withJitter(ttl));
        } catch (Exception ignore) {}
    }

    @Override
    public void evict(CacheKey key) {
        redis.delete(key.asString());
    }
}
