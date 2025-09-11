package com.loopers.ranking;


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
    

    public double calculateScore(double baseScore, Double customWeight) {
        double weight = customWeight != null ? customWeight : this.defaultWeight;
        return baseScore * weight;
    }
}
