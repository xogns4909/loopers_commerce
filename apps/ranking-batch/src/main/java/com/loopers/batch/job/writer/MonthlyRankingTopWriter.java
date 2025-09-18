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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class MonthlyRankingTopWriter implements ItemWriter<MonthlyRankingMV>, StepExecutionListener {

    private final MonthlyRankingRepository repository;

    private final PriorityQueue<MonthlyRankingMV> topProducts = new PriorityQueue<>(
        Comparator.comparing(MonthlyRankingMV::getRankingScore)
    );

    @Value("#{jobParameters['targetDate']}")
    private String targetDate;

    private static final int TOP_N = 100;

    @Override
    @Transactional
    public void beforeStep(StepExecution stepExecution) {
        LocalDate date = LocalDate.parse(targetDate);
        repository.deleteByTargetDate(date);
        log.info("월간 Top {} 기존 데이터 삭제 완료 - 날짜: {}", TOP_N, date);
        topProducts.clear();
        log.info("월간 Top {} 랭킹 수집 시작 - 대상 날짜: {}", TOP_N, date);
    }

    @Override
    public void write(Chunk<? extends MonthlyRankingMV> chunk) {
        for (MonthlyRankingMV item : chunk) {
            if (topProducts.size() < TOP_N) {
                topProducts.offer(item);
            } else {
                MonthlyRankingMV lowest = topProducts.peek();
                if (lowest != null && item.getRankingScore().compareTo(lowest.getRankingScore()) > 0) {
                    topProducts.poll();
                    topProducts.offer(item);
                }
            }
        }
        log.debug("월간 Top 후보 수집 - PQ 크기: {}", topProducts.size());
    }

    @Override
    @Transactional
    public ExitStatus afterStep(StepExecution stepExecution) {
        List<MonthlyRankingMV> finalTop = new ArrayList<>(topProducts);
        finalTop.sort(Comparator.comparing(MonthlyRankingMV::getRankingScore).reversed());

        // rank_no 채움 (reflection 금지)
        int rank = 1;
        for (MonthlyRankingMV mv : finalTop) {
            mv.setRankNo(rank++);
        }

        if (!finalTop.isEmpty()) {
            repository.saveAll(finalTop);
        }

        int saved = finalTop.size();
        log.info("월간 Top {} 저장 완료 - 저장 건수: {}", TOP_N, saved);
        if (saved > 0) {
            BigDecimal topScore = finalTop.get(0).getRankingScore();
            BigDecimal bottomScore = finalTop.get(saved - 1).getRankingScore();
            log.info("월간 점수 범위: {} (1위) ~ {} ({}위)", topScore, bottomScore, saved);
        }

        stepExecution.getExecutionContext().putInt("monthlyTopSaved", saved);
        return ExitStatus.COMPLETED;
    }
}
