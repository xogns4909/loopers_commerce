package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductInfo;
import lombok.Setter;

import com.loopers.application.product.ProductInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingEntry {

    private Long productId;
    private Double score;
    private ProductInfo productInfo; // 상세(이름/가격/브랜드 등)
}
