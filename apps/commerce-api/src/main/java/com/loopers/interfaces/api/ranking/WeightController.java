package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.ranking.RankingEventType;
import com.loopers.ranking.WeightManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 랭킹 가중치 관리 API
 * 
 * 기능:
 * 1. 현재 가중치 조회
 * 2. 개별 가중치 변경
 * 3. 가중치 정보 (버전, 캐시 상태 등)
 * 4. 긴급 리셋
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ranking/weights")
@RequiredArgsConstructor
public class WeightController {
    
    private final WeightManager weightManager;
    
    /**
     * 현재 가중치 조회
     */
    @GetMapping
    public ApiResponse<Map<RankingEventType, Double>> getWeights() {
        Map<RankingEventType, Double> weights = weightManager.getAllWeights();
        log.info("가중치 조회 완료: {}", weights);
        return ApiResponse.success(weights);
    }
    
    /**
     * 가중치 상세 정보 조회 (버전, 캐시 상태 포함)
     */
    @GetMapping("/info")
    public ApiResponse<WeightManager.WeightInfo> getWeightInfo() {
        WeightManager.WeightInfo info = weightManager.getWeightInfo();
        return ApiResponse.success(info);
    }
    
    /**
     * 개별 가중치 변경
     */
    @PutMapping("/{eventType}")
    public ApiResponse<Object> updateWeight(
        @PathVariable RankingEventType eventType,
        @RequestBody UpdateWeightRequest request
    ) {
        double oldWeight = weightManager.getWeight(eventType);
        
        if (request.weight() < -1.0 || request.weight() > 10.0) {
            return ApiResponse.success("가중치는 -1.0 ~ 10.0 범위여야 합니다.");
        }
        
        weightManager.updateWeight(eventType, request.weight());
        
        log.info("가중치 변경 완료 - {}: {} -> {}, 변경자: {}", 
                eventType, oldWeight, request.weight(), request.updatedBy());
        
        return ApiResponse.success();
    }
    
    /**
     * 가중치 전체 리셋 (긴급 상황용)
     */
    @PostMapping("/reset")
    public ApiResponse<Object> resetWeights(@RequestBody ResetWeightsRequest request) {
        log.warn("가중치 전체 리셋 요청 - 사유: {}, 요청자: {}", request.reason(), request.requestedBy());
        
        weightManager.resetToDefaults();
        
        return ApiResponse.success();
    }
    
    /**
     * 가중치 변경 요청 모델
     */
    public record UpdateWeightRequest(
        double weight,
        String updatedBy,
        String reason
    ) {}
    
    /**
     * 가중치 리셋 요청 모델
     */
    public record ResetWeightsRequest(
        String reason,
        String requestedBy
    ) {}
}
