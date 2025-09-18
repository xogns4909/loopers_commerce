package com.loopers.application.ranking;

import com.loopers.application.ranking.dto.RankingResult;
import com.loopers.domain.ranking.MonthlyRankingMV;
import com.loopers.domain.ranking.WeeklyRankingMV;
import com.loopers.infrastructure.ranking.MonthlyRankingRepository;
import com.loopers.infrastructure.ranking.WeeklyRankingRepository;
import com.loopers.interfaces.api.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 랭킹 조회 서비스 구현체
 * - Redis(일간), DB(주간/월간) 데이터 조회
 * - Fallback 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final WeeklyRankingRepository weeklyRepo;
    private final MonthlyRankingRepository monthlyRepo;

    @Override
    public RankingResult getDailyRankings(LocalDate targetDate, int start, int end) {
        return fetchFromRedisWithFallback(targetDate, start, end);
    }

    @Override
    public RankingResult getWeeklyRankings(LocalDate targetDate, int page, int size) {
        return fetchFromWeeklyDb(targetDate, page, size);
    }

    @Override
    public RankingResult getMonthlyRankings(LocalDate targetDate, int page, int size) {
        return fetchFromMonthlyDb(targetDate, page, size);
    }

    @Override
    public ProductRankingInfo getProductDailyRanking(Long productId, LocalDate targetDate) {
        String member = "product:" + productId;
        Long rank = null;
        Double score = null;
        LocalDate actual = targetDate;

        // 3일간 fallback 조회
        for (int i = 0; i < 3; i++) {
            LocalDate d = targetDate.minusDays(i);
            String key = dailyKey(d);
            Double s = redisTemplate.opsForZSet().score(key, member);
            if (s != null) {
                score = s;
                Long r = redisTemplate.opsForZSet().reverseRank(key, member);
                rank = (r == null) ? null : r + 1;
                actual = d;
                break;
            }
        }

        return new ProductRankingInfo(productId, actual, rank, score);
    }

    // ===== Private Methods =====

    private RankingResult fetchFromRedisWithFallback(LocalDate targetDate, int start, int end) {
        for (int i = 0; i < 3; i++) {
            LocalDate d = targetDate.minusDays(i);
            String key = dailyKey(d);
            Set<TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            if (tuples != null && !tuples.isEmpty()) {
                List<RankingEntry> entries = tuples.stream()
                    .map(t -> RankingEntry.builder()
                        .productId(parseProductId(t.getValue()))
                        .score(t.getScore() == null ? 0.0 : t.getScore())
                        .productInfo(null)
                        .build())
                    .filter(e -> e.getProductId() != null)
                    .collect(Collectors.toList());
                String src = switch (i) {
                    case 0 -> "redis";
                    case 1 -> "redis-fallback-yesterday";
                    default -> "redis-fallback-day-before";
                };
                return RankingResult.builder()
                    .actualDate(d)
                    .entries(entries)
                    .source(src)
                    .build();
            }
        }
        return RankingResult.builder()
            .actualDate(targetDate)
            .entries(List.of())
            .source("redis-empty")
            .build();
    }

    private RankingResult fetchFromWeeklyDb(LocalDate targetDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<WeeklyRankingMV> rows = weeklyRepo.findByTargetDateOrderByRankNoAsc(targetDate, pageable);

        LocalDate actual = targetDate;
        String source = "db-weekly";

        if (rows == null || rows.isEmpty()) {
            LocalDate latest = weeklyRepo.findLatestTargetDate(targetDate);
            if (latest == null) {
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-weekly-empty")
                    .build();
            }
            rows = weeklyRepo.findByTargetDateOrderByRankNoAsc(latest, pageable);
            actual = latest;
            source = "db-weekly-fallback";
        }

        List<RankingEntry> entries = rows.stream()
            .map(this::toEntryFromWeekly)
            .collect(Collectors.toList());

        return RankingResult.builder()
            .actualDate(actual)
            .entries(entries)
            .source(source)
            .build();
    }

    private RankingResult fetchFromMonthlyDb(LocalDate targetDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<MonthlyRankingMV> rows = monthlyRepo.findByTargetDateOrderByRankNoAsc(targetDate, pageable);

        LocalDate actual = targetDate;
        String source = "db-monthly";

        if (rows == null || rows.isEmpty()) {
            LocalDate latest = monthlyRepo.findLatestTargetDate(targetDate);
            if (latest == null) {
                return RankingResult.builder()
                    .actualDate(targetDate)
                    .entries(List.of())
                    .source("db-monthly-empty")
                    .build();
            }
            rows = monthlyRepo.findByTargetDateOrderByRankNoAsc(latest, pageable);
            actual = latest;
            source = "db-monthly-fallback";
        }

        List<RankingEntry> entries = rows.stream()
            .map(this::toEntryFromMonthly)
            .collect(Collectors.toList());

        return RankingResult.builder()
            .actualDate(actual)
            .entries(entries)
            .source(source)
            .build();
    }

    private RankingEntry toEntryFromWeekly(WeeklyRankingMV mv) {
        return RankingEntry.builder()
            .productId(mv.getProductId())
            .score(mv.getRankingScore())
            .productInfo(null)
            .build();
    }

    private RankingEntry toEntryFromMonthly(MonthlyRankingMV mv) {
        return RankingEntry.builder()
            .productId(mv.getProductId())
            .score(mv.getRankingScore())
            .productInfo(null)
            .build();
    }

    private String dailyKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private Long parseProductId(String member) {
        if (member == null || !member.startsWith("product:")) return null;
        try {
            return Long.parseLong(member.substring(8));
        } catch (NumberFormatException e) {
            log.warn("잘못된 product member 형식: {}", member);
            return null;
        }
    }
}
