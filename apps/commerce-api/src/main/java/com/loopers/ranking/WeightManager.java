package com.loopers.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class WeightManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis 키 (Commerce-Streamer와 동일)
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
        // 로컬 캐시 초기 로드 (Redis 없으면 기본값 사용)
        refreshCacheFromRedis();
        log.info("가중치 관리자 초기화 완료 - Commerce-API");
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
     * 가중치 실시간 변경 (Redis + 로컬 캐시 즉시 갱신)
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
        
        log.info("가중치 변경됨 - {}: {} -> {} (Redis 반영 완료)", eventType, oldWeight, newWeight);
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
