package com.loopers.batch.repository;

import com.loopers.batch.domain.entity.DailyRankingMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyRankingRepository extends JpaRepository<DailyRankingMV, Long> {
    
    /**
     * 특정 날짜 범위의 일별 랭킹 조회 (배치에서만 사용)
     */
    List<DailyRankingMV> findByStatDateBetweenOrderByProductId(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 날짜의 데이터 삭제 (재실행용)
     */
    void deleteByStatDate(LocalDate statDate);
}
