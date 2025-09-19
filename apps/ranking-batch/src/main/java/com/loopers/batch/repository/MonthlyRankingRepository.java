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


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MonthlyRankingMV m WHERE m.targetDate = :targetDate")
    int deleteByTargetDate(@Param("targetDate") LocalDate targetDate);
}
