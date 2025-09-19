package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.PeriodType;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.dto.RankingResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingFacade rankingFacade;

    /**
     * 랭킹 조회
     * period: daily | weekly | monthly (default: daily)
     * date: yyyyMMdd (없으면 오늘)
     */
    @GetMapping
    public ApiResponse<RankingResponse> getRankings(
        @RequestParam(defaultValue = "daily") String period,
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "1") int page
    ) {
        final int MAX_PAGE_SIZE = 100;
        page = Math.max(page, 1);
        size = Math.max(size, 1);
        size = Math.min(size, MAX_PAGE_SIZE);

        PeriodType p = PeriodType.from(period);
        LocalDate targetDate = parseDate(date);

        log.info("랭킹 조회 요청 - period: {}, date: {}, size: {}, page: {}", p, targetDate, size, page);

        RankingResult result = rankingFacade.getRankings(p, targetDate, page, size);

        if (result.isEmpty()) {
            log.info("랭킹 데이터가 없음 - period: {}, date: {}", p, targetDate);
            return ApiResponse.success(RankingResponse.empty(targetDate));
        }

        RankingResponse response = RankingResponse.of(
            result.getActualDate(),
            result.getEntries(),
            result.getSource(),
            page,
            size
        );

        return ApiResponse.success(response);
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ProductRankingInfo> getProductRanking(
        @PathVariable Long productId,
        @RequestParam(required = false) String date
    ) {
        LocalDate targetDate = parseDate(date);

        ProductRankingInfo info = rankingFacade.getProductRanking(productId, targetDate);

        log.info("상품 랭킹 조회 - productId: {}, date: {}, rank: {}, score: {}",
            productId, targetDate, info.getRank(), info.getScore());

        return ApiResponse.success(info);
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            log.warn("잘못된 날짜 형식: {}, 오늘 날짜 사용", date);
            return LocalDate.now();
        }
    }
}
