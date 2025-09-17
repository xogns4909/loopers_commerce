package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.MonthlyRankingMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyRankingRepository extends JpaRepository<MonthlyRankingMV, Long> {
    
    /**
     * 특정 년월의 랭킹 조회 (순위 순)
     */
    List<MonthlyRankingMV> findByYearMonthOrderByRankNo(String yearMonth);
    
    /**
     * 특정 년월의 데이터 삭제 (재실행용)
     */
    void deleteByYearMonth(String yearMonth);
}
