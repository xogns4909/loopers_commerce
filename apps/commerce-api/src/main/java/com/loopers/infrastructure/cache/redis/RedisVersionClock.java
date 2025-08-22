package com.loopers.infrastructure.cache.redis;

import com.loopers.application.product.VersionClock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisVersionClock implements VersionClock {

    private final RedisTemplate<String, String> redis;
    private static final String NS_KEY_PREFIX = "shop:cache:ns:";

    public RedisVersionClock(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public String current(String ns) {
        String key = NS_KEY_PREFIX + ns;
        String v = redis.opsForValue().get(key);
        return v == null ? "0" : v;
    }

    @Override
    public void bump(String ns) {
        String key = NS_KEY_PREFIX + ns;
        redis.opsForValue().increment(key);
    }
}


