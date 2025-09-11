package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.ranking.RankingEventType;
import com.loopers.ranking.WeightManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * 랭킹 테스트 API (개발/QA 환경용)
 * 실시간 가중치 변경 효과 확인 및 디버깅
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dev/ranking")
@RequiredArgsConstructor
public class RankingTestController {

    private final RedisTemplate<String, String> redisTemplate;
    private final WeightManager weightManager;

    /**
     * 현재 가중치 빠른 조회
     */
    @GetMapping("/weights")
    public ApiResponse<Map<RankingEventType, Double>> getCurrentWeights() {
        return ApiResponse.success(weightManager.getAllWeights());
    }

    /**
     * 특정 날짜 랭킹 Raw 데이터 조회 (디버깅용)
     */
    @GetMapping("/raw/{date}")
    public ApiResponse<RawRankingData> getRawRanking(
        @PathVariable String date,
        @RequestParam(defaultValue = "10") int limit
    ) {

            LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
            String key = "ranking:all:" + date;

            Set<String> members = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
            Long totalCount = redisTemplate.opsForZSet().zCard(key);
            Long ttl = redisTemplate.getExpire(key);

            RawRankingData rawData = new RawRankingData(
                targetDate,
                key,
                members,
                totalCount != null ? totalCount : 0,
                ttl != null ? ttl : -1
            );

            return ApiResponse.success(rawData);


    }

    /**
     * 테스트용 점수 직접 추가
     */
    @PostMapping("/test-score")
    public ApiResponse<Object> addTestScore(@RequestBody TestScoreRequest request) {
        String key = "ranking:all:" + request.date();
        String member = "product:" + request.productId();

        redisTemplate.opsForZSet().incrementScore(key, member, request.score());
        redisTemplate.expire(key, java.time.Duration.ofDays(2));

        log.info("테스트 점수 추가: {} -> {} (+{})", member, key, request.score());
        return ApiResponse.success();
    }

    /**
     * 오늘 날짜로 빠른 테스트 데이터 생성
     */
    @PostMapping("/quick-setup")
    public ApiResponse<Object> quickSetupTodayRanking() {
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "ranking:all:" + today;

        // 샘플 랭킹 데이터 생성
        redisTemplate.opsForZSet().add(key, "product:1", 150.7);  // VIEW(0.1×5) + LIKE(0.2×1) + ORDER(0.7×200) = 140.7
        redisTemplate.opsForZSet().add(key, "product:2", 85.4);   // VIEW(0.1×10) + LIKE(0.2×2) + ORDER(0.7×120) = 85.4
        redisTemplate.opsForZSet().add(key, "product:3", 70.3);   // VIEW(0.1×15) + LIKE(0.2×4) = 70.3
        redisTemplate.opsForZSet().add(key, "product:4", 45.2);   // VIEW(0.1×20) + LIKE(0.2×1) = 45.2
        redisTemplate.opsForZSet().add(key, "product:5", 28.5);   // VIEW(0.1×25) + LIKE(0.2×15) = 28.5

        redisTemplate.expire(key, java.time.Duration.ofDays(2));

        log.info("오늘 날짜({}) 랭킹 테스트 데이터 생성 완료", today);
        return ApiResponse.success();
    }

    /**
     * Fallback 테스트용 어제 데이터 생성
     */
    @PostMapping("/setup-yesterday")
    public ApiResponse<Object> setupYesterdayRanking() {
        String yesterday = java.time.LocalDate.now().minusDays(1)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "ranking:all:" + yesterday;

        // 어제 랭킹 데이터
        redisTemplate.opsForZSet().add(key, "product:10", 300.0);
        redisTemplate.opsForZSet().add(key, "product:11", 250.0);
        redisTemplate.opsForZSet().add(key, "product:12", 200.0);

        redisTemplate.expire(key, java.time.Duration.ofDays(2));

        log.info("어제 날짜({}) 랭킹 테스트 데이터 생성 완료", yesterday);
        return ApiResponse.success();
    }

    /**
     * 랭킹 키 정리 (테스트용)
     */
    @DeleteMapping("/clean/{date}")
    public ApiResponse<Object> cleanRankingData(@PathVariable String date) {
        String key = "ranking:all:" + date;
        Boolean deleted = redisTemplate.delete(key);

        log.info("랭킹 데이터 정리: {} -> {}", key, deleted);
        return ApiResponse.success();
    }

    /**
     * 가중치 빠른 변경 (테스트용)
     */
    @PutMapping("/quick-weight/{eventType}/{weight}")
    public ApiResponse<Object> quickUpdateWeight(
        @PathVariable RankingEventType eventType,
        @PathVariable double weight
    ) {
        weightManager.updateWeight(eventType, weight);
        log.info("가중치 빠른 변경: {} -> {}", eventType, weight);
        return ApiResponse.success();
    }

    /**
     * Raw 랭킹 데이터 응답 모델
     */
    public record RawRankingData(
        LocalDate date,
        String redisKey,
        Set<String> topMembers,
        long totalCount,
        long ttlSeconds
    ) {}

    /**
     * 테스트 점수 추가 요청 모델
     */
    public record TestScoreRequest(
        String date,        // yyyyMMdd
        Long productId,
        double score
    ) {}
}
