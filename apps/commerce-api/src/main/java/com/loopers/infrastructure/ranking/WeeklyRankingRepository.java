package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRankingMV;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyRankingRepository extends JpaRepository<WeeklyRankingMV, Long> {
    List<WeeklyRankingMV> findByTargetDateOrderByRankNoAsc(LocalDate targetDate, Pageable pageable);
    Optional<WeeklyRankingMV> findByProductIdAndTargetDate(Long productId, LocalDate targetDate);

    @Query("select max(w.targetDate) from WeeklyRankingMV w where w.targetDate <= :targetDate")
    LocalDate findLatestTargetDate(LocalDate targetDate);
}
