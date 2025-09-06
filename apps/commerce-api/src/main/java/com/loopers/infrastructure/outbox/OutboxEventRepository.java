package com.loopers.infrastructure.outbox;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAt(OutboxEvent.OutboxStatus status);

    @Query("""
        SELECT e
          FROM OutboxEvent e
         WHERE e.status = 'NEW'
            OR (e.status = 'FAILED' AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now))
         ORDER BY e.createdAt ASC
    """)
    List<OutboxEvent> findTop100ReadyToSend(@Param("now") ZonedDateTime now);

    @Modifying
    @Query("""
        UPDATE OutboxEvent e
           SET e.status = 'SENDING',
               e.updatedAt = :now
         WHERE e.id IN :ids
           AND e.status IN ('NEW','FAILED')
    """)
    int claimEventsForSending(@Param("ids") List<Long> ids,
        @Param("now") ZonedDateTime now);

    @Modifying
    @Query("""
        DELETE FROM OutboxEvent e
         WHERE e.status = 'PUBLISHED'
           AND e.createdAt < :cutoff
    """)
    int softDeleteOldPublishedEvents(@Param("cutoff") ZonedDateTime cutoff);

    // 누락된 메소드들 추가
    boolean existsByMessageId(String messageId);
    
    long countByStatus(OutboxEvent.OutboxStatus status);
}
