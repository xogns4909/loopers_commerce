package com.loopers.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.loopers.batch",     // 배치 모듈
    "com.loopers.config.jpa" // JPA 모듈의 Configuration (DataSourceConfig 포함)
})
@EnableJpaRepositories(basePackages = "com.loopers.batch.repository")
@EntityScan(basePackages = "com.loopers.batch.domain.entity")
@EnableBatchProcessing
@EnableScheduling
public class RankingBatchApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RankingBatchApplication.class, args);
    }
}
