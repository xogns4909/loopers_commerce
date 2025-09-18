package com.loopers.batch.config;

import com.loopers.batch.domain.dto.WeeklyMetricsDto;
import com.loopers.batch.domain.entity.WeeklyRankingMV;
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
public class WeeklyRankingJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    @Value("${batch.chunk-size:1000}")
    private int chunkSize;
    
    @Bean
    public Job weeklyRankingJob(Step weeklyRankingStep) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .listener(weeklyJobListener())
                .start(weeklyRankingStep)
                .build();
    }
    
    @Bean  
    public Step weeklyRankingStep(
        ItemReader<WeeklyMetricsDto> weeklyMetricsReader,
        ItemProcessor<WeeklyMetricsDto, WeeklyRankingMV> weeklyRankingProcessor,
        ItemWriter<WeeklyRankingMV> weeklyRankingTopWriter
    ) {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<WeeklyMetricsDto, WeeklyRankingMV>chunk(chunkSize, transactionManager)
                .reader(weeklyMetricsReader)
                .processor(weeklyRankingProcessor)
                .writer(weeklyRankingTopWriter)
                .build();
    }
    
    private JobExecutionListener weeklyJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                JobParameters params = jobExecution.getJobParameters();
                String weekStartDate = params.getString("weekStartDate");
                
                log.info("=== 주간 랭킹 배치 시작 ===");
                log.info("주 시작일: {}", weekStartDate);
                log.info("청크 크기: {}", chunkSize);
            }
            
            @Override
            public void afterJob(JobExecution jobExecution) {
                long executionTime = java.time.Duration.between(
                    jobExecution.getStartTime(),
                    jobExecution.getEndTime()
                ).toSeconds();
                
                log.info("=== 주간 랭킹 배치 완료 ===");
                log.info("실행 시간: {}초", executionTime);
                log.info("최종 상태: {}", jobExecution.getStatus());
            }
        };
    }
}
