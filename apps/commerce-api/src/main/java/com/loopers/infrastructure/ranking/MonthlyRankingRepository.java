package com.loopers.infrastructure.ranking;


import com.loopers.domain.ranking.MonthlyRankingMV;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MonthlyRankingRepository extends JpaRepository<MonthlyRankingMV, Long> {
    List<MonthlyRankingMV> findByTargetDateOrderByRankNoAsc(LocalDate targetDate, Pageable pageable);
    Optional<MonthlyRankingMV> findByProductIdAndTargetDate(Long productId, LocalDate targetDate);

    @Query("select max(m.targetDate) from MonthlyRankingMV m where m.targetDate <= :targetDate")
    LocalDate findLatestTargetDate(LocalDate targetDate);
}
