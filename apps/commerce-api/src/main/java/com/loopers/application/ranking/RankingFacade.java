package com.loopers.application.ranking;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.ranking.dto.RankingResult;
import com.loopers.interfaces.api.ranking.ProductRankingInfo;
import com.loopers.interfaces.api.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductFacade productFacade;


    public RankingResult getRankings(PeriodType period, LocalDate targetDate, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        log.info("랭킹 조회 시작 - period={}, date={}, page={}, size={}", period, targetDate, page, size);

        RankingResult result = switch (period) {
            case DAILY -> rankingService.getDailyRankings(targetDate, start, end);
            case WEEKLY -> rankingService.getWeeklyRankings(targetDate, page, size);
            case MONTHLY -> rankingService.getMonthlyRankings(targetDate, page, size);
        };

        // 상품 정보 보강
        if (!result.isEmpty()) {
            enrichEntriesWithProductInfo(result.getEntries());
        }

        return result;
    }


    public ProductRankingInfo getProductRanking(Long productId, LocalDate targetDate) {
        RankingService.ProductRankingInfo rankingInfo = 
            rankingService.getProductDailyRanking(productId, targetDate);

        ProductInfo productInfo = null;
        try {
            Map<Long, ProductInfo> map = productFacade.getProductInfoMap(List.of(productId));
            productInfo = map.get(productId);
        } catch (Exception e) {
            log.warn("상품 상세 조회 실패 productId={}", productId, e);
        }

        return ProductRankingInfo.builder()
            .productId(rankingInfo.productId())
            .date(rankingInfo.date())
            .rank(rankingInfo.rank())
            .score(rankingInfo.score())
            .productInfo(productInfo)
            .build();
    }

    /**
     * 랭킹 엔트리에 상품 정보 보강
     */
    private void enrichEntriesWithProductInfo(List<RankingEntry> entries) {
        if (entries == null || entries.isEmpty()) return;

        List<Long> productIds = entries.stream()
            .map(RankingEntry::getProductId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        try {
            Map<Long, ProductInfo> infoMap = productFacade.getProductInfoMap(productIds);
            for (RankingEntry entry : entries) {
                entry.setProductInfo(infoMap.get(entry.getProductId()));
            }
        } catch (Exception ex) {
            log.warn("상품정보 배치 조회 실패", ex);
        }
    }
}
