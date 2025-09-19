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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeeklyRankingTopWriter implements ItemWriter<WeeklyRankingMV>, StepExecutionListener {
    
    private final WeeklyRankingRepository repository;
    
    // 최소 힙: 가장 낮은 점수가 top에 위치 (100개 넘으면 제거)
    private final PriorityQueue<WeeklyRankingMV> topProducts = new PriorityQueue<>(
        Comparator.comparing(WeeklyRankingMV::getRankingScore)
    );
    
    @Value("#{jobParameters['targetDate']}")
    private String targetDate;
    
    private static final int TOP_N = 100;
    
    @Override
    @Transactional
    public void beforeStep(StepExecution stepExecution) {
        if (targetDate == null || targetDate.isBlank()) {
            throw new IllegalArgumentException("jobParameters['targetDate']는 필수입니다");
        }
        
        LocalDate date;
        try {
            date = LocalDate.parse(targetDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("targetDate 형식이 잘못되었습니다(예: 2025-09-18): " + targetDate, e);
        }
        
        repository.deleteByTargetDate(date);
        log.info("기존 데이터 삭제 완료 - 날짜: {}", date);
        
        topProducts.clear();
        log.info("주간 Top {} 랭킹 수집 시작 - 대상 날짜: {}", TOP_N, date);
    }
    
    @Override
    public void write(Chunk<? extends WeeklyRankingMV> chunk) {
        for (WeeklyRankingMV item : chunk) {
            
            if (topProducts.size() < TOP_N) {
                // 아직 100개 미만이면 추가
                topProducts.offer(item);
            } else {
                // 100개 찼으면 최소값과 비교
                WeeklyRankingMV lowest = topProducts.peek();
                if (lowest != null && item.getRankingScore().compareTo(lowest.getRankingScore()) > 0) {
                    topProducts.poll(); // 최소값 제거
                    topProducts.offer(item); // 새 값 추가
                }
            }
        }
        
        log.debug("현재 Top 후보 수집 중 - PQ 크기: {}", topProducts.size());
    }


    @Override
    @Transactional
    public ExitStatus afterStep(StepExecution stepExecution) {
        List<WeeklyRankingMV> finalTop100 = new ArrayList<>(topProducts);


        finalTop100.sort(Comparator
            .comparing(WeeklyRankingMV::getRankingScore).reversed()
            .thenComparing(WeeklyRankingMV::getOrderCount, Comparator.reverseOrder())
            .thenComparing(WeeklyRankingMV::getLikeCount, Comparator.reverseOrder())
            .thenComparing(WeeklyRankingMV::getViewCount, Comparator.reverseOrder())
            .thenComparing(WeeklyRankingMV::getProductId)
        );


        int rank = 0;
        BigDecimal prevScore = null;
        for (WeeklyRankingMV cur : finalTop100) {
            if (prevScore == null || cur.getRankingScore().compareTo(prevScore) != 0) {
                rank++;
                prevScore = cur.getRankingScore();
            }
            cur.assignRank(rank);
        }


        if (!finalTop100.isEmpty()) {
            repository.saveAll(finalTop100);
        }

        log.info("주간 Top {} 랭킹 저장 완료 - 저장 건수: {}", TOP_N, finalTop100.size());
        if (!finalTop100.isEmpty()) {
            BigDecimal topScore = finalTop100.get(0).getRankingScore();
            BigDecimal bottomScore = finalTop100.get(finalTop100.size() - 1).getRankingScore();
            log.info("점수 범위: {} (1위) ~ {} ({}위)", topScore, bottomScore, finalTop100.size());
        }

        stepExecution.getExecutionContext().putInt("topRankingSaved", finalTop100.size());
        return ExitStatus.COMPLETED;
    }


}
