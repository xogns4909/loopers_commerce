package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.WeeklyRankingMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRankingMV, Long> {
    
    /**
     * 특정 주차의 랭킹 조회 (순위 순)
     */
    List<WeeklyRankingMV> findByYearWeekOrderByRankNo(String yearWeek);
    
    /**
     * 특정 주차의 데이터 삭제 (재실행용)
     */
    void deleteByYearWeek(String yearWeek);
}
