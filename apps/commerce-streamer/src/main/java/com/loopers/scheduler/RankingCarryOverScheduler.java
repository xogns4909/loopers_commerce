package com.loopers.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 랭킹 Carry-Over 스케줄러
 * 
 * 콜드 스타트 문제 해결:
 * - 매일 23:50에 전날 TOP 100의 10%를 오늘 시드로 복사
 * - 신상품과 기존 인기 상품의 균형 유지
 * - Redis ZUNIONSTORE로 서버사이드에서 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingCarryOverScheduler {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final double CARRY_WEIGHT = 0.1;  // 10% 가중치
    private static final int TOP_N = 100;            // 상위 100개만 carry
    
    /**
     * 매일 23:50에 carry-over 실행
     * 자정 직전에 다음날 시드 준비
     */
    @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
    public void performCarryOver() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        String todayKey = generateRankingKey(today);
        String tomorrowKey = generateRankingKey(tomorrow);
        String tempKey = "temp:carry:" + tomorrow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        try {
            log.info("랭킹 Carry-Over 시작: {} -> {}", today, tomorrow);
            
            // 1단계: 오늘 랭킹이 존재하는지 확인
            Long todayCount = redisTemplate.opsForZSet().zCard(todayKey);
            if (todayCount == null || todayCount == 0) {
                log.warn("오늘 랭킹 데이터가 없어 Carry-Over 건너뜀: {}", today);
                return;
            }
            
            // 2단계: 상위 TOP_N개 추출하여 임시 키에 저장
            Set<String> topProducts = redisTemplate.opsForZSet().reverseRange(todayKey, 0, TOP_N - 1);
            if (topProducts == null || topProducts.isEmpty()) {
                log.warn("상위 상품이 없어 Carry-Over 건너뜀: {}", today);
                return;
            }
            
            // 3단계: 단순히 모든 값에 가중치 적용하여 복사
            // ZUNIONSTORE 대신 개별 복사 + 가중치 적용
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> todayRanking = 
                redisTemplate.opsForZSet().reverseRangeWithScores(todayKey, 0, TOP_N - 1);
            
            if (todayRanking != null && !todayRanking.isEmpty()) {
                for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : todayRanking) {
                    Double score = tuple.getScore();
                    if (score != null) {
                        redisTemplate.opsForZSet().add(tomorrowKey, tuple.getValue(), score * CARRY_WEIGHT);
                    }
                }
            }
            
            // 4단계: 이미 TOP_N개만 복사했으므로 별도 처리 불필요
            
            // 5단계: TTL 설정 (2일)
            redisTemplate.expire(tomorrowKey, java.time.Duration.ofDays(2));
            
            // 6단계: 임시 키 정리
            redisTemplate.delete(tempKey);
            
            Long carriedCount = redisTemplate.opsForZSet().zCard(tomorrowKey);
            log.info("랭킹 Carry-Over 완료: {} -> {} ({}개 상품, 가중치: {})", 
                    today, tomorrow, carriedCount, CARRY_WEIGHT);
                    
        } catch (Exception e) {
            log.error("랭킹 Carry-Over 실패: {} -> {}", today, tomorrow, e);
            // 실패해도 서비스에는 영향 없음 (다음날 점진적으로 랭킹 형성)
        }
    }
    
    /**
     * 수동 Carry-Over (관리자 API용)
     */
    public boolean performCarryOver(LocalDate sourceDate, LocalDate targetDate) {
        String sourceKey = generateRankingKey(sourceDate);
        String targetKey = generateRankingKey(targetDate);
        
        try {
            log.info("수동 랭킹 Carry-Over 시작: {} -> {}", sourceDate, targetDate);
            
            Long sourceCount = redisTemplate.opsForZSet().zCard(sourceKey);
            if (sourceCount == null || sourceCount == 0) {
                log.warn("소스 랭킹 데이터가 없음: {}", sourceDate);
                return false;
            }
            
            // 기존 타겟 데이터와 병합 (기존 데이터 유지)
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> sourceRanking = 
                redisTemplate.opsForZSet().reverseRangeWithScores(sourceKey, 0, TOP_N - 1);
            
            if (sourceRanking != null && !sourceRanking.isEmpty()) {
                for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : sourceRanking) {
                    Double score = tuple.getScore();
                    if (score != null) {
                        redisTemplate.opsForZSet().add(targetKey, tuple.getValue(), score * CARRY_WEIGHT);
                    }
                }
            }
            
            Long carriedCount = redisTemplate.opsForZSet().zCard(targetKey);
            log.info("수동 랭킹 Carry-Over 완료: {} -> {} ({}개 상품)", 
                    sourceDate, targetDate, carriedCount);
            
            return true;
            
        } catch (Exception e) {
            log.error("수동 랭킹 Carry-Over 실패: {} -> {}", sourceDate, targetDate, e);
            return false;
        }
    }
    
    private String generateRankingKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
