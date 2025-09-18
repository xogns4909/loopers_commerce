package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.MonthlyRankingMV;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyRankingRepository extends JpaRepository<MonthlyRankingMV, Long> {


    @Modifying
    @Query("DELETE FROM MonthlyRankingMV w WHERE w.targetDate = :targetDate")
    void deleteByTargetDate(@Param("targetDate") LocalDate targetDate);
}
