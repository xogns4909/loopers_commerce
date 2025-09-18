package com.loopers.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 기반 랭킹 가중치 관리자
 * 
 * 핵심 설계:
 * 1. Redis HASH로 가중치 저장: ranking:weights
 * 2. 버전 관리로 Blue/Green 가중치 전환 지원
 * 3. 로컬 캐시로 성능 최적화 (1분 TTL)
 * 4. 실시간 가중치 변경 및 즉시 반영
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeightManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis 키
    private static final String WEIGHTS_KEY = "ranking:weights";
    private static final String WEIGHTS_VERSION_KEY = "ranking:weights:version";
    
    // 로컬 캐시 (성능 최적화)
    private final Map<RankingEventType, Double> cachedWeights = new ConcurrentHashMap<>();
    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_TTL_MS = 60_000; // 1분
    
    // Fallback 기본값
    @Value("${app.ranking.score.weights.view:0.1}") 
    private double defaultViewWeight;
    @Value("${app.ranking.score.weights.like:0.2}") 
    private double defaultLikeWeight;
    @Value("${app.ranking.score.weights.unlike:-0.2}") 
    private double defaultUnlikeWeight;
    @Value("${app.ranking.score.weights.order:0.7}") 
    private double defaultOrderWeight;
    
    @PostConstruct
    public void initialize() {
        if (!redisTemplate.hasKey(WEIGHTS_KEY)) {
            initializeDefaultWeights();
        }
        
        // 로컬 캐시 초기 로드
        refreshCacheFromRedis();
        
        log.info("가중치 관리자 초기화 완료 - Redis 기반");
    }
    
    /**
     * 특정 이벤트 타입의 가중치 조회 (캐시 우선)
     */
    public double getWeight(RankingEventType eventType) {
        refreshCacheIfNeeded();
        
        Double weight = cachedWeights.get(eventType);
        if (weight != null) {
            return weight;
        }
        
        // 캐시 미스 시 기본값 반환
        double defaultWeight = getDefaultWeight(eventType);
        cachedWeights.put(eventType, defaultWeight);
        return defaultWeight;
    }
    
    /**
     * 가중치 실시간 변경 (Redis + 로컬 캐시 즉시 갱신 + 기존 데이터 재계산)
     */
    public void updateWeight(RankingEventType eventType, double newWeight) {
        double oldWeight = getWeight(eventType);
        
        // Redis 업데이트
        redisTemplate.opsForHash().put(WEIGHTS_KEY, eventType.name(), String.valueOf(newWeight));
        
        // 버전 업데이트 (변경 추적용)
        redisTemplate.opsForValue().set(WEIGHTS_VERSION_KEY, String.valueOf(Instant.now().toEpochMilli()));
        
        // 로컬 캐시 즉시 갱신
        cachedWeights.put(eventType, newWeight);
        lastCacheUpdate = System.currentTimeMillis();
        
        // 기존 랭킹 데이터 재계산 (오늘, 어제 데이터)
        recalculateExistingRankings(eventType, oldWeight, newWeight);
        
        log.info("가중치 변경 완료 - {}: {} -> {} (기존 데이터 재계산 포함)", eventType, oldWeight, newWeight);
    }
    
    /**
     * 가중치 변경 시 기존 랭킹 데이터 재계산
     */
    private void recalculateExistingRankings(RankingEventType eventType, double oldWeight, double newWeight) {
        if (oldWeight == 0.0) {
            log.warn("기존 가중치가 0이므로 재계산 건너뜀: {}", eventType);
            return;
        }
        
        double ratio = newWeight / oldWeight;
        LocalDate today = LocalDate.now();
        
        // 오늘과 어제 데이터만 재계산 (너무 오래된 데이터는 자연스럽게 대체되도록)
        for (int i = 0; i < 2; i++) {
            LocalDate targetDate = today.minusDays(i);
            recalculateRankingForDate(targetDate, ratio, eventType);
        }
    }
    
    /**
     * 특정 날짜의 랭킹 데이터를 새로운 가중치 비율로 재계산
     */
    private void recalculateRankingForDate(LocalDate date, double ratio, RankingEventType eventType) {
        String key = "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        try {
            // 현재 랭킹 데이터 조회
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> allEntries = 
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
            
            if (allEntries == null || allEntries.isEmpty()) {
                log.debug("재계산할 랭킹 데이터가 없음: {}", date);
                return;
            }
            
            // 모든 항목에 비율 적용
            for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> entry : allEntries) {
                String member = entry.getValue();
                Double currentScore = entry.getScore();
                
                if (currentScore != null) {
                    double newScore = currentScore * ratio;
                    redisTemplate.opsForZSet().add(key, member, newScore);
                }
            }
            
            log.info("랭킹 재계산 완료: {} - {} 이벤트 가중치 변경 (비율: {:.2f}, 대상: {}개)", 
                    date, eventType, ratio, allEntries.size());
                    
        } catch (Exception e) {
            log.error("랭킹 재계산 실패: {} - {}", date, eventType, e);
        }
    }
    
    /**
     * 모든 가중치 정보 조회
     */
    public Map<RankingEventType, Double> getAllWeights() {
        refreshCacheIfNeeded();
        return Map.copyOf(cachedWeights);
    }
    
    /**
     * 가중치 버전 정보 조회 (관리자 API용)
     */
    public WeightInfo getWeightInfo() {
        refreshCacheIfNeeded();
        
        String version = redisTemplate.opsForValue().get(WEIGHTS_VERSION_KEY);
        long versionTimestamp = 0;
        
        if (version != null) {
            try {
                versionTimestamp = Long.parseLong(version);
            } catch (NumberFormatException e) {
                log.warn("가중치 버전 파싱 실패: {}", version);
            }
        }
        
        return WeightInfo.of(Map.copyOf(cachedWeights), versionTimestamp, lastCacheUpdate);
    }
    
    /**
     * 가중치 전체 리셋 (긴급 상황용)
     */
    public void resetToDefaults() {
        log.warn("가중치 전체 리셋 실행");
        
        redisTemplate.delete(WEIGHTS_KEY);
        redisTemplate.delete(WEIGHTS_VERSION_KEY);
        
        initializeDefaultWeights();
        refreshCacheFromRedis();
        
        log.info("가중치 리셋 완료 - 기본값으로 복원");
    }
    
    // === Private Methods ===
    
    private void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate > CACHE_TTL_MS) {
            refreshCacheFromRedis();
        }
    }
    
    private void refreshCacheFromRedis() {
        try {
            Map<Object, Object> redisWeights = redisTemplate.opsForHash().entries(WEIGHTS_KEY);
            
            cachedWeights.clear();
            
            for (RankingEventType eventType : RankingEventType.values()) {
                String weightStr = (String) redisWeights.get(eventType.name());
                double weight;
                
                if (weightStr != null) {
                    try {
                        weight = Double.parseDouble(weightStr);
                    } catch (NumberFormatException e) {
                        log.warn("가중치 파싱 실패: {} = {}, 기본값 사용", eventType, weightStr);
                        weight = getDefaultWeight(eventType);
                    }
                } else {
                    weight = getDefaultWeight(eventType);
                }
                
                cachedWeights.put(eventType, weight);
            }
            
            lastCacheUpdate = System.currentTimeMillis();
            log.debug("가중치 캐시 갱신 완료: {}", cachedWeights);
            
        } catch (Exception e) {
            log.error("Redis에서 가중치 로드 실패, 기본값 사용", e);
            loadDefaultWeightsToCache();
        }
    }
    
    private void initializeDefaultWeights() {
        redisTemplate.opsForHash().put(WEIGHTS_KEY, RankingEventType.PRODUCT_VIEWED.name(), String.valueOf(defaultViewWeight));
        redisTemplate.opsForHash().put(WEIGHTS_KEY, RankingEventType.PRODUCT_LIKED.name(), String.valueOf(defaultLikeWeight));
        redisTemplate.opsForHash().put(WEIGHTS_KEY, RankingEventType.PRODUCT_UNLIKED.name(), String.valueOf(defaultUnlikeWeight));
        redisTemplate.opsForHash().put(WEIGHTS_KEY, RankingEventType.ORDER_CREATED.name(), String.valueOf(defaultOrderWeight));
        
        redisTemplate.opsForValue().set(WEIGHTS_VERSION_KEY, String.valueOf(Instant.now().toEpochMilli()));
        
        log.info("Redis에 기본 가중치 저장 완료");
    }
    
    private void loadDefaultWeightsToCache() {
        cachedWeights.clear();
        cachedWeights.put(RankingEventType.PRODUCT_VIEWED, defaultViewWeight);
        cachedWeights.put(RankingEventType.PRODUCT_LIKED, defaultLikeWeight);
        cachedWeights.put(RankingEventType.PRODUCT_UNLIKED, defaultUnlikeWeight);
        cachedWeights.put(RankingEventType.ORDER_CREATED, defaultOrderWeight);
        
        lastCacheUpdate = System.currentTimeMillis();
    }
    
    private double getDefaultWeight(RankingEventType eventType) {
        return switch (eventType) {
            case PRODUCT_VIEWED -> defaultViewWeight;
            case PRODUCT_LIKED -> defaultLikeWeight;
            case PRODUCT_UNLIKED -> defaultUnlikeWeight;
            case ORDER_CREATED -> defaultOrderWeight;
        };
    }
    
    /**
     * 가중치 정보 응답 모델
     */
    public record WeightInfo(
        Map<RankingEventType, Double> weights,
        long versionTimestamp,
        long lastCacheUpdate
    ) {
        public static WeightInfo of(Map<RankingEventType, Double> weights, long versionTimestamp, long lastCacheUpdate) {
            return new WeightInfo(weights, versionTimestamp, lastCacheUpdate);
        }
    }
}
