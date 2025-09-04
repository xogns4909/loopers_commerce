package com.loopers.repository;

import com.loopers.entity.ProcessedEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    Optional<ProcessedEvent> findByMessageId(String messageId);

    @Modifying
    @Query("""
        UPDATE ProcessedEvent e
           SET e.status = 'PROCESSED', e.processedAt = :now
         WHERE e.messageId = :messageId
           AND e.status = 'PROCESSING'
    """)
    int markProcessed(@Param("messageId") String messageId, @Param("now") ZonedDateTime now);

    @Modifying
    @Query("""
        UPDATE ProcessedEvent e
           SET e.status = 'FAILED', e.processedAt = :now
         WHERE e.messageId = :messageId
           AND e.status = 'PROCESSING'
    """)
    int markFailed(@Param("messageId") String messageId, @Param("now") ZonedDateTime now);

    @Modifying
    @Query("""
        DELETE FROM ProcessedEvent e
         WHERE e.status = 'PROCESSING' AND e.startedAt < :cutoff
    """)
    int cleanupStaleProcessing(@Param("cutoff") ZonedDateTime cutoff);


    default boolean tryInsertProcessing(String messageId, String eventType, String correlationId) {
        try {
            saveAndFlush(ProcessedEvent.start(messageId, eventType, correlationId));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
