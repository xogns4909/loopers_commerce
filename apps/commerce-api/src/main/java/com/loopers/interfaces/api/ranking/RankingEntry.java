package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductInfo;

/**
 * 랭킹 엔트리 (상품 정보 포함)
 */
public record RankingEntry(
    Long productId,
    Double score,
    ProductInfo productInfo  // 상품 메타데이터 (이름, 가격, 이미지 등)
) {}
