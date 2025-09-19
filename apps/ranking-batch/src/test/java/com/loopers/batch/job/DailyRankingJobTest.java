package com.loopers.batch.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Daily Ranking Batch Job 테스트
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class DailyRankingJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dailyRankingJob")
    private Job dailyRankingJob;

    @Test
    void testDailyRankingJob() throws Exception {
        // JobLauncherTestUtils에 Job 설정
        jobLauncherTestUtils.setJob(dailyRankingJob);

        // Given
        String testDate = "2025-09-11"; // 실제 데이터가 있는 날짜

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", testDate)
                .addLong("timestamp", System.currentTimeMillis()) // 유니크한 파라미터 추가
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        System.out.println("배치 실행 결과: " + jobExecution.getExitStatus());
//        System.out.println("처리 시간: " + (jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime()) + "ms");
    }
}
