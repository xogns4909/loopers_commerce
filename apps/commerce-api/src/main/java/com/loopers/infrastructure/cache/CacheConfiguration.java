package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.cache.*;
import com.loopers.application.cache.PageView.PageView;
import com.loopers.application.product.ProductInfo;
import com.loopers.infrastructure.cache.redis.RedisCacheStore;
import com.loopers.infrastructure.cache.redis.codec.JacksonCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CacheConfiguration {

    @Bean
    public CacheStore<ProductInfo> productDetailStore(
        @Qualifier("redisTemplate") RedisTemplate<String,String> redis,
        ObjectMapper om, CachePolicy policy) {
        return new RedisCacheStore<>(
            redis,
            new JacksonCodec<>(om, new TypeReference<ProductInfo>(){}),
            policy
        );
    }

    @Bean
    public CacheStore<PageView<ProductInfo>> productListStore(
        @Qualifier("redisTemplate") RedisTemplate<String,String> redis,
        ObjectMapper om, CachePolicy policy) {
        return new RedisCacheStore<>(
            redis,
            new JacksonCodec<>(om, new TypeReference<PageView<ProductInfo>>(){}),
            policy
        );
    }
}
