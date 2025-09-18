package com.loopers.batch.job;

import com.loopers.batch.RankingBatchApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주간 랭킹 배치 테스트
 */
@SpringBootTest(classes = RankingBatchApplication.class)
@SpringBatchTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
class WeeklyRankingJobTest {


    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    @Qualifier("weeklyRankingJob")
    private Job weeklyRankingJob;
    
    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(weeklyRankingJob);
    }

    @Test
    void testWeeklyRankingJob() throws Exception {
        // Given
        String targetDate = "2025-09-13"; // 슬라이딩 윈도우 기준 날짜
        
//        log.info("=== 주간 랭킹 배치 테스트 시작 ===");
//        log.info("대상 날짜: {}", targetDate);
        
        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                jobLauncherTestUtils.getUniqueJobParametersBuilder()
                        .addString("targetDate", targetDate)
                        .toJobParameters()
        );
        
        // Then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
//        log.info("=== 주간 랭킹 배치 테스트 완료 ===");
//        log.info("실행 결과: {}", jobExecution.getExitStatus().getExitCode());
    }
}
