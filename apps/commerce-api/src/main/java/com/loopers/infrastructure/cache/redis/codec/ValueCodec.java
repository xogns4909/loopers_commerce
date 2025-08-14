package com.loopers.infrastructure.cache.redis.codec;

public interface ValueCodec<V> {
    String serialize(V value) throws Exception;
    V deserialize(String raw) throws Exception;
}
