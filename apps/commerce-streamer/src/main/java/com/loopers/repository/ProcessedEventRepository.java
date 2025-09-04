package com.loopers.repository;

import com.loopers.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
    
    /**
     * 이벤트가 이미 처리되었는지 확인
     */
    boolean existsByMessageId(String messageId);
    
    /**
     * 오래된 처리 기록 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM ProcessedEvent p WHERE p.processedAt < :before")
    void deleteOldProcessedEvents(@Param("before") LocalDateTime before);
}
