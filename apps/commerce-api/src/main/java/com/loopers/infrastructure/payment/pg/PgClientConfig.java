package com.loopers.infrastructure.payment.pg;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PgClientConfig {
    
    @Value("${pg.timeout.connect:1000}")
    private int connectTimeout;
    
    @Value("${pg.timeout.read:3000}")
    private int readTimeout;
    
    @Bean
    public Request.Options pgRequestOptions() {
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true);
    }
    
    @Bean
    public Logger.Level pgFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }
    
    @Bean
    public Retryer pgRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
