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
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RankingBatchApplication.class)
@SpringBatchTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
class MonthlyRankingJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("monthlyRankingJob")
    private Job monthlyRankingJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(monthlyRankingJob);
    }

    @Test
    void testMonthlyRankingJob() throws Exception {
        String targetDate = "2025-09-30";

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
            jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("targetDate", targetDate)
                .toJobParameters()
        );

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }
}
