package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.WeeklyRankingMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRankingMV, Long> {
    

    List<WeeklyRankingMV> findByTargetDateOrderByRankNoAsc(LocalDate targetDate, Pageable pageable);
    
    Optional<WeeklyRankingMV> findByProductIdAndTargetDate(Long productId, LocalDate targetDate);
    
    @Query("select max(w.targetDate) from WeeklyRankingMV w where w.targetDate <= :targetDate")
    LocalDate findLatestTargetDate(@Param("targetDate") LocalDate targetDate);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WeeklyRankingMV w WHERE w.targetDate = :targetDate")
    int deleteByTargetDate(@Param("targetDate") LocalDate targetDate);
}
