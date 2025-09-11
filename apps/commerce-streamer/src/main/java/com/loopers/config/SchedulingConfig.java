package com.loopers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 설정
 * 
 * 활성화되는 스케줄러:
 * - RankingCarryOverScheduler: 매일 23:50 carry-over 실행
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Spring Boot의 기본 스케줄링 설정 사용
    // 필요 시 ThreadPoolTaskScheduler 커스터마이징 가능
}
