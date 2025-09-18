package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
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
    
    @GetMapping
    public ApiResponse<RankingResponse> getRankings(
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "1") int page
    ) {
        LocalDate targetDate = parseDate(date);
        
        log.info("랭킹 조회 요청 - date: {}, size: {}, page: {}", targetDate, size, page);
        
        RankingFacade.RankingResult result = rankingFacade.getRankings(targetDate, page, size);
        
        if (result.isEmpty()) {
            log.info("랭킹 데이터가 없음: {}", targetDate);
            return ApiResponse.success(RankingResponse.empty(targetDate));
        }
        
        RankingResponse response = RankingResponse.of(
            result.actualDate(),
            result.entries(), 
            result.source(),
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
        
        log.info("상품 랭킹 조회: {} - 순위: {}, 점수: {}", productId, info.rank(), info.score());
        
        return ApiResponse.success(info);
    }
    
    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            log.warn("잘못된 날짜 형식: {}, 오늘 날짜 사용", date);
            return LocalDate.now();
        }
    }
}
