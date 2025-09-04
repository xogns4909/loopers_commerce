package com.loopers.repository;

import com.loopers.entity.DeadLetterEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, Long> {

    Optional<DeadLetterEvent> findByMessageId(String messageId);

    Page<DeadLetterEvent> findByStatusOrderByLastFailedAtDesc(DeadLetterEvent.Status status, Pageable pageable);

    Page<DeadLetterEvent> findByEventTypeAndStatusOrderByLastFailedAtDesc(String eventType, DeadLetterEvent.Status status, Pageable pageable);

    Page<DeadLetterEvent> findByFailureReasonAndStatusOrderByLastFailedAtDesc(DeadLetterEvent.FailureReason failureReason, DeadLetterEvent.Status status, Pageable pageable);

    @Query("""
        SELECT d FROM DeadLetterEvent d 
        WHERE d.status = :status 
        AND d.lastFailedAt BETWEEN :startDate AND :endDate
        ORDER BY d.lastFailedAt DESC
    """)
    Page<DeadLetterEvent> findByStatusAndDateRange(
            @Param("status") DeadLetterEvent.Status status,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable);

    @Query("""
        SELECT d.eventType as eventType, d.failureReason as failureReason, COUNT(d) as count
        FROM DeadLetterEvent d 
        WHERE d.status = 'DEAD' 
        AND d.lastFailedAt >= :since
        GROUP BY d.eventType, d.failureReason
        ORDER BY count DESC
    """)
    List<DeadLetterEventSummary> getFailureSummary(@Param("since") ZonedDateTime since);

    @Modifying
    @Query("""
        UPDATE DeadLetterEvent d 
        SET d.status = 'RESOLVED', 
            d.resolvedAt = :now, 
            d.resolvedBy = :resolvedBy,
            d.resolutionNotes = :notes
        WHERE d.id IN :ids
    """)
    int markAsResolved(@Param("ids") List<Long> ids, 
                       @Param("now") ZonedDateTime now,
                       @Param("resolvedBy") String resolvedBy,
                       @Param("notes") String notes);

    @Query("""
        SELECT COUNT(d) FROM DeadLetterEvent d 
        WHERE d.status = 'DEAD' 
        AND d.lastFailedAt >= :since
    """)
    long countDeadEventsSince(@Param("since") ZonedDateTime since);

    @Modifying
    @Query("""
        DELETE FROM DeadLetterEvent d 
        WHERE d.status = :status 
        AND d.resolvedAt < :cutoff
    """)
    int deleteByStatusAndResolvedAtBefore(@Param("status") DeadLetterEvent.Status status, @Param("cutoff") ZonedDateTime cutoff);

    long countByStatus(DeadLetterEvent.Status status);

    interface DeadLetterEventSummary {
        String getEventType();
        DeadLetterEvent.FailureReason getFailureReason();
        Long getCount();
    }
}
