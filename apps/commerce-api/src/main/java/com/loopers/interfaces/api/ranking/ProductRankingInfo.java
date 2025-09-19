package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductInfo;
import java.time.LocalDate;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRankingInfo {
    private Long productId;
    private LocalDate date;
    private Long rank;          // 1-based, null이면 없음
    private Double score;       // null이면 없음
    private ProductInfo productInfo;

    public boolean isInRanking() {
        return rank != null && score != null;
    }
}

