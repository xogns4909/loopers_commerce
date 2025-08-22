package com.loopers.infrastructure.cache.redis.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        return new LettuceConnectionFactory(createRedisConfiguration(redisProperties));
    }

    private RedisStandaloneConfiguration createRedisConfiguration(RedisProperties redisProperties) {
        RedisStandaloneConfiguration redisConfig =
            new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

        Optional.ofNullable(redisProperties.getPassword())
            .ifPresent(password -> redisConfig.setPassword(RedisPassword.of(password)));

        Optional.of(redisProperties.getDatabase())
            .ifPresent(redisConfig::setDatabase);

        return redisConfig;
    }
    @Primary
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        configureRedisSerializers(redisTemplate, stringSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private void configureRedisSerializers(RedisTemplate<String, String> redisTemplate,
        RedisSerializer<String> serializer) {
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
    }

    @Bean
    @Primary
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
        // Page 인터페이스를 PageImpl로 역직렬화하도록 설정
        mapper.addMixIn(Page.class, PageMixin.class);
        
        return mapper;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Page 인터페이스를 PageImpl로 역직렬화하도록 설정
        mapper.addMixIn(Page.class, PageMixin.class);

        // 기타 필요한 설정들
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @JsonDeserialize(as = PageImpl.class)
    public abstract class PageMixin {}

}
