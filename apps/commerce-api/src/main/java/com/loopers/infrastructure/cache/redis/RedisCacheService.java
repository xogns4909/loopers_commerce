package com.loopers.infrastructure.cache.redis;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.infrastructure.cache.core.CachePolicy;
import com.loopers.infrastructure.cache.core.CacheService;
import com.loopers.infrastructure.cache.core.TypeRef;
import com.loopers.application.product.VersionClock;
import com.loopers.infrastructure.cache.util.JsonCodec;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redis;
    private final JsonCodec codec;
    private final SingleFlightRegistry singleFlight = new SingleFlightRegistry();
    private final VersionClock versionClock;



    @Override
    public <T> T getOrLoad(CacheKey key, TypeRef<T> typeRef, Loader<T> loader, CachePolicy policy) {
        final String redisKey = key.asString(); // KeyBuilder에서 완성된 키 사용
        String cached = redis.opsForValue().get(redisKey);
        if (cached != null) {
            T decoded = decodeOrEvict(cached, typeRef, redisKey);
            if (decoded != null) return decoded;
        }

        // 2) SingleFlight로 중복 로드 방지
        CompletableFuture<String> flight = singleFlight.computeIfAbsent(redisKey, () ->
            CompletableFuture.supplyAsync(() -> {
                String retry = redis.opsForValue().get(redisKey);
                if (retry != null) {
                    T decoded = decodeOrEvict(retry, typeRef, redisKey);
                    if (decoded != null) return retry;
                }

                try {
                    T value = loader.load();
                    writeCache(redisKey, value, policy);
                    return codec.encode(value);
                } catch (CoreException ce) {
                    throw ce;
                } catch (Exception ex) {
                    throw new CoreException(ErrorType.INTERNAL_ERROR);
                }
            })
        );


        try {
            String json = flight.join();
            return codec.decode(json, typeRef);
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause() != null ? ce.getCause() : ce;

            if (cause instanceof CoreException core) {
                throw core;
            }


            try {
                return loader.load();
            } catch (CoreException core2) {
                throw core2;
            } catch (Exception ex) {
                throw new CoreException(ErrorType.INTERNAL_ERROR);
            }
        }
    }

    @Override
    public <T> void preload(CacheKey key, TypeRef<T> typeRef, Loader<T> loader, CachePolicy policy) {
        final String redisKey = key.asString(); // KeyBuilder에서 완성된 키 사용
        try {
            T value = loader.load();
            writeCache(redisKey, value, policy);
            if (log.isDebugEnabled()) log.debug("Preload ok: {}", redisKey);
        } catch (CoreException ignore) {
           throw ignore;
        } catch (Exception ex) {
            throw new CoreException(ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public void evict(CacheKey key) {
        final String redisKey = key.asString(); 
        safeDelete(redisKey);
    }


    @Override
    public void bumpNamespace(String namespace) {
        versionClock.bump(namespace);
    }


    private <T> T decodeOrEvict(String json, TypeRef<T> typeRef, String redisKey) {
        try {
            return codec.decode(json, typeRef);
        } catch (Exception decodeEx) {
            safeDelete(redisKey);
            return null;
        }
    }

    private <T> void writeCache(String redisKey, T value, CachePolicy policy) {
        Duration ttl = jitter(value == null ? policy.ttlNull() : policy.ttlDetail(), 0.1);
        String json = codec.encode(value);
        if (ttl != null) {
            redis.opsForValue().set(redisKey, json, ttl);
        } else {
            redis.opsForValue().set(redisKey, json);
        }
    }

    private void safeDelete(String redisKey) {
        try { redis.delete(redisKey); } catch (Exception ignore) { }
    }

    private Duration jitter(Duration base, double ratio) {
        if (base == null) return null;
        long ms = base.toMillis();
        long delta = (long) (ms * ratio);
        long j = ThreadLocalRandom.current().nextLong(-delta, delta + 1);
        return Duration.ofMillis(Math.max(1000, ms + j));
    }
    
}
