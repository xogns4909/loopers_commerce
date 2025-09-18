package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.WeeklyRankingMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRankingMV, Long> {
    

    @Modifying
    @Query("DELETE FROM WeeklyRankingMV w WHERE w.targetDate = :targetDate")
    void deleteByTargetDate(@Param("targetDate") LocalDate targetDate);
}
