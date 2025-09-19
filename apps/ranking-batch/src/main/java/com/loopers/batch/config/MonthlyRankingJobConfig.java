package com.loopers.batch.config;

import com.loopers.batch.domain.dto.MonthlyMetricsDto;
import com.loopers.batch.domain.entity.MonthlyRankingMV;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MonthlyRankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Value("${batch.chunk-size:1000}")
    private int chunkSize;

    @Bean
    public Job monthlyRankingJob(Step monthlyRankingStep) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
            .listener(monthlyJobListener())
            .start(monthlyRankingStep)
            .build();
    }

    @Bean
    public Step monthlyRankingStep(
        ItemReader<MonthlyMetricsDto> monthlyMetricsReader,
        ItemProcessor<MonthlyMetricsDto, MonthlyRankingMV> monthlyRankingProcessor,
        // 필요에 따라 단순저장/Top저장 택1
        // ItemWriter<MonthlyRankingMV> monthlyRankingWriter
        ItemWriter<MonthlyRankingMV> monthlyRankingTopWriter
    ) {
        return new StepBuilder("monthlyRankingStep", jobRepository)
            .<MonthlyMetricsDto, MonthlyRankingMV>chunk(chunkSize, transactionManager)
            .reader(monthlyMetricsReader)
            .processor(monthlyRankingProcessor)
            .writer(monthlyRankingTopWriter) // Top 100 저장 사용
            .build();
    }

    private JobExecutionListener monthlyJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                String monthStart = jobExecution.getJobParameters().getString("monthStartDate");
                log.info("=== 월간 랭킹 배치 시작 ===");
                log.info("월 시작일(옵션): {}", monthStart);
                log.info("청크 크기: {}", chunkSize);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                long sec = java.time.Duration.between(
                    jobExecution.getStartTime(), jobExecution.getEndTime()).toSeconds();
                log.info("=== 월간 랭킹 배치 완료 ===");
                log.info("실행 시간: {}초", sec);
                log.info("최종 상태: {}", jobExecution.getStatus());
            }
        };
    }
}
