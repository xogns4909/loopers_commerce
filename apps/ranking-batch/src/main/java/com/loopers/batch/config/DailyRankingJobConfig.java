package com.loopers.batch.config;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import com.loopers.batch.domain.entity.DailyRankingMV;
import com.loopers.batch.job.processor.DailyRankingProcessor;
import com.loopers.batch.job.reader.EventMetricsCursorReader;
import com.loopers.batch.job.writer.DailyRankingWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDateTime;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class DailyRankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EventMetricsCursorReader readerFactory;
    private final DailyRankingProcessor processor;
    private final DailyRankingWriter writer;

    @Value("${batch.chunk-size:1000}")
    private int chunkSize;

    @Bean
    public Job dailyRankingJob() {
        return new JobBuilder("dailyRankingJob", jobRepository)
            .start(dailyRankingStep())
            .incrementer(new RunIdIncrementer())
            .listener(dailyRankingJobListener())
            .build();
    }

    @Bean
    public Step dailyRankingStep() {
        return new StepBuilder("dailyRankingStep", jobRepository)
            .<DailyMetricsDto, DailyRankingMV>chunk(chunkSize, transactionManager)
            .reader(readerFactory.dailyMetricsReader(null, null)) // 실제 실행 시점에 주입됨
            .processor(processor)
            .writer(writer)
            .listener(writer) // Writer가 StepExecutionListener도 구현
            .build();
    }

    @Bean
    public JobExecutionListener dailyRankingJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                String targetDate = jobExecution.getJobParameters().getString("targetDate");
                log.info("=== 일별 랭킹 배치 시작 ===");
                log.info("대상 날짜: {}", targetDate);
                log.info("청크 크기: {}", chunkSize);
                jobExecution.getExecutionContext().put("startTime", LocalDateTime.now());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                LocalDateTime startTime = (LocalDateTime) jobExecution.getExecutionContext().get("startTime");
                Duration duration = Duration.between(startTime, LocalDateTime.now());

                log.info("=== 일별 랭킹 배치 완료 ===");
                log.info("실행 시간: {}초", duration.toSeconds());
                log.info("최종 상태: {}", jobExecution.getStatus());

                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    // 처리된 건수 조회
                    jobExecution.getStepExecutions().forEach(step -> {
                        int processedCount = step.getExecutionContext().getInt("processedCount", 0);
                        log.info("처리 완료 - Step: {}, 처리 건수: {}", step.getStepName(), processedCount);
                    });
                }
            }
        };
    }
}
