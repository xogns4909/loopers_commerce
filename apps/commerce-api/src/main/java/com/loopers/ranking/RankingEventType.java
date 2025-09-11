package com.loopers.ranking;

/**
 * 랭킹 계산에 사용되는 이벤트 타입
 * 각 이벤트별로 다른 가중치를 적용하여 최종 점수 계산
 */
public enum RankingEventType {
    PRODUCT_VIEWED(0.1),       // 상품 조회: 낮은 가중치 (높은 빈도)
    PRODUCT_LIKED(0.2),        // 상품 좋아요: 중간 가중치  
    PRODUCT_UNLIKED(-0.2),     // 좋아요 취소: 음수 보정
    ORDER_CREATED(0.7);        // 주문 생성: 높은 가중치 (실제 구매)
    
    private final double defaultWeight;
    
    RankingEventType(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }
    
    public double getDefaultWeight() {
        return defaultWeight;
    }
    
    /**
     * 이벤트 타입에 따른 점수 계산
     * @param baseScore 기본 점수 (주문의 경우 금액, 나머지는 1)
     * @param customWeight 커스텀 가중치 (null인 경우 기본값 사용)
     * @return 최종 점수
     */
    public double calculateScore(double baseScore, Double customWeight) {
        double weight = customWeight != null ? customWeight : this.defaultWeight;
        return baseScore * weight;
    }
}
