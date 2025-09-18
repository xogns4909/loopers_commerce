package com.loopers.batch.job.writer;

import com.loopers.batch.domain.entity.MonthlyRankingMV;
import com.loopers.batch.repository.MonthlyRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class MonthlyRankingWriter implements ItemWriter<MonthlyRankingMV>, StepExecutionListener {

    private final MonthlyRankingRepository repository;
    private final AtomicInteger processedCount = new AtomicInteger(0);

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    private LocalDate snapshotDate;

    @Override
    @Transactional
    public void beforeStep(StepExecution stepExecution) {
        this.snapshotDate = LocalDate.parse(targetDateParam);
        repository.deleteByTargetDate(snapshotDate);
        log.info("월간 기존 데이터 삭제 완료 - 날짜: {}", snapshotDate);
        processedCount.set(0);
        log.info("월간 랭킹 저장 시작 - 대상 날짜: {}", snapshotDate);
    }

    @Override
    @Transactional
    public void write(Chunk<? extends MonthlyRankingMV> chunk) {
        if (chunk.isEmpty()) return;
        repository.saveAll(chunk.getItems());
        int current = processedCount.addAndGet(chunk.size());
        log.debug("월간 랭킹 저장 진행 - 현재 {}건 처리", current);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        int total = processedCount.get();
        log.info("월간 랭킹 저장 완료 - 총 처리 건수: {}", total);
        stepExecution.getExecutionContext().putInt("monthlyProcessedCount", total);
        return ExitStatus.COMPLETED;
    }
}
