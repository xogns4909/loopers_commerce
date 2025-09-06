package com.loopers.scheduler;

import com.loopers.entity.DeadLetterEvent;
import com.loopers.entity.ProcessedEvent;
import com.loopers.repository.DeadLetterEventRepository;
import com.loopers.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterCleanupScheduler {

    private final ProcessedEventRepository processedEventRepository;
    private final DeadLetterEventRepository deadLetterEventRepository;

    @Value("${app.cleanup.processed-events.retention-days:30}")
    private int processedEventRetentionDays;

    @Value("${app.cleanup.dead-letter-events.retention-days:90}")
    private int deadLetterRetentionDays;

    @Value("${app.cleanup.stale-processing.timeout-hours:2}")
    private int staleProcessingTimeoutHours;


    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldProcessedEvents() {
        try {
            ZonedDateTime cutoff = ZonedDateTime.now().minusDays(processedEventRetentionDays);

            int deletedCount = processedEventRepository.deleteByStatusAndProcessedAtBefore(
                    ProcessedEvent.Status.PROCESSED, cutoff);
                    
            if (deletedCount > 0) {
                log.info("Cleaned up {} old processed events older than {} days", 
                        deletedCount, processedEventRetentionDays);
            }
            
        } catch (Exception e) {
            log.error("Failed to cleanup old processed events", e);
        }
    }

    /**
     * 매주 일요일 새벽 3시에 실행 - 오래된 해결된 DLT 이벤트 정리
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupOldResolvedDeadLetterEvents() {
        try {
            ZonedDateTime cutoff = ZonedDateTime.now().minusDays(deadLetterRetentionDays);
            
            int deletedCount = deadLetterEventRepository.deleteByStatusAndResolvedAtBefore(
                    DeadLetterEvent.Status.RESOLVED, cutoff);
                    
            if (deletedCount > 0) {
                log.info("Cleaned up {} old resolved dead letter events older than {} days", 
                        deletedCount, deadLetterRetentionDays);
            }
            
        } catch (Exception e) {
            log.error("Failed to cleanup old resolved dead letter events", e);
        }
    }


    @Scheduled(fixedRate = 1800000) // 30분
    @Transactional
    public void cleanupStaleProcessingEvents() {
        try {
            ZonedDateTime cutoff = ZonedDateTime.now().minusHours(staleProcessingTimeoutHours);
            
            int cleanedCount = processedEventRepository.cleanupStaleProcessing(cutoff);
            
            if (cleanedCount > 0) {
                log.warn("Cleaned up {} stale processing events that were stuck for more than {} hours", 
                        cleanedCount, staleProcessingTimeoutHours);
            }
            
        } catch (Exception e) {
            log.error("Failed to cleanup stale processing events", e);
        }
    }


    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional(readOnly = true)
    public void logDeadLetterStatistics() {
        try {
            long totalDead = deadLetterEventRepository.countByStatus(DeadLetterEvent.Status.DEAD);
            long totalInvestigating = deadLetterEventRepository.countByStatus(DeadLetterEvent.Status.INVESTIGATING);
            long totalResolved = deadLetterEventRepository.countByStatus(DeadLetterEvent.Status.RESOLVED);
            
            log.info("=== Daily DLT Statistics ===");
            log.info("Dead Events: {}", totalDead);
            log.info("Investigating: {}", totalInvestigating);
            log.info("Resolved: {}", totalResolved);
            
            // 최근 24시간 실패 원인별 통계
            ZonedDateTime since24h = ZonedDateTime.now().minusHours(24);
            List<DeadLetterEventRepository.DeadLetterEventSummary> failures = 
                deadLetterEventRepository.getFailureSummary(since24h);
                
            log.info("=== Last 24h Failure Summary ===");
            failures.forEach(failure -> 
                log.info("{} - {}: {} events", 
                    failure.getEventType(), 
                    failure.getFailureReason(), 
                    failure.getCount())
            );
            
        } catch (Exception e) {
            log.error("Failed to log DLT statistics", e);
        }
    }
}
