package com.loopers.batch.job.writer;

import com.loopers.batch.domain.entity.WeeklyRankingMV;
import com.loopers.batch.repository.WeeklyRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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
public class WeeklyRankingWriter implements ItemWriter<WeeklyRankingMV>, StepExecutionListener {
    
    private final WeeklyRankingRepository repository;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    
    @Value("#{jobParameters['targetDate']}")
    private String targetDate;
    
    @Override
    @Transactional
    public void beforeStep(StepExecution stepExecution) {
        LocalDate date = LocalDate.parse(targetDate);
        
        // 기존 데이터 삭제 (멱등성 보장)
        repository.deleteByTargetDate(date);
        log.info("기존 데이터 삭제 완료 - 날짜: {}", date);
        
        processedCount.set(0);
        log.info("주간 랭킹 저장 시작 - 대상 날짜: {}", date);
    }
    
    @Override
    @Transactional
    public void write(Chunk<? extends WeeklyRankingMV> chunk) {
        if (chunk.isEmpty()) {
            return;
        }
        
        // 배치 저장
        repository.saveAll(chunk.getItems());
        
        int currentCount = processedCount.addAndGet(chunk.size());
        log.debug("주간 랭킹 저장 진행 - 현재 {}건 처리됨", currentCount);
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        int totalProcessed = processedCount.get();
        log.info("주간 랭킹 저장 완료 - 총 처리 건수: {}", totalProcessed);

        stepExecution.getExecutionContext().putInt("processedCount", totalProcessed);
        
        return ExitStatus.COMPLETED;
    }
}
