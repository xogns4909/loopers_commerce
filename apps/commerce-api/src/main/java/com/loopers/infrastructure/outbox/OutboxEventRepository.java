package com.loopers.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;


@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {


    @Modifying
    @Query("""
        UPDATE OutboxEvent o 
        SET o.status = 'SENDING', o.updatedAt = CURRENT_TIMESTAMP
        WHERE o.id IN :ids AND o.status = 'NEW'
        """)
    int claimEventsForSending(@Param("ids") List<Long> ids);

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAt(OutboxEvent.OutboxStatus status);


    boolean existsByMessageId(String messageId);


    @Query("SELECT COUNT(o) FROM OutboxEvent o WHERE o.status = :status AND o.deletedAt IS NULL")
    long countByStatus(@Param("status") OutboxEvent.OutboxStatus status);


    @Query("""
        SELECT o FROM OutboxEvent o 
        WHERE o.status = 'FAILED' 
        AND o.retryCount < 3
        AND o.updatedAt < :retryThreshold
        AND o.deletedAt IS NULL
        ORDER BY o.createdAt ASC
        """)
    List<OutboxEvent> findRetryableEvents(@Param("retryThreshold") ZonedDateTime retryThreshold);


    @Modifying
    @Query("""
        UPDATE OutboxEvent o 
        SET o.deletedAt = CURRENT_TIMESTAMP
        WHERE o.status = 'PUBLISHED' 
        AND o.updatedAt < :cutoffDate
        AND o.deletedAt IS NULL
        """)
    int softDeleteOldPublishedEvents(@Param("cutoffDate") ZonedDateTime cutoffDate);


    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.deletedAt < :cutoffDate")
    int hardDeleteOldEvents(@Param("cutoffDate") ZonedDateTime cutoffDate);
}
