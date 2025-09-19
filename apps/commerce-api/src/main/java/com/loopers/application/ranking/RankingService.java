package com.loopers.application.ranking;

import com.loopers.application.ranking.dto.RankingResult;

import java.time.LocalDate;

/**
 * 랭킹 조회 서비스 인터페이스
 */
public interface RankingService {


    RankingResult getDailyRankings(LocalDate targetDate, int start, int end);


    RankingResult getWeeklyRankings(LocalDate targetDate, int page, int size);


    RankingResult getMonthlyRankings(LocalDate targetDate, int page, int size);

    ProductRankingInfo getProductDailyRanking(Long productId, LocalDate targetDate);


    record ProductRankingInfo(Long productId, LocalDate date, Long rank, Double score) {}
}
