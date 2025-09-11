package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @GetMapping
    public ApiResponse<List<RankingEntry>> getRankings(
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "0") int page
    ) {
        // 전날 확정 랭킹 제공
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String sumKey = generateSumKey(yesterday);
        
        int start = page * size;
        int end = start + size - 1;
        
        log.info("Ranking request - date: {}, size: {}, page: {}", yesterday, size, page);
        
        Set<String> products = redisTemplate.opsForZSet().reverseRange(sumKey, start, end);
        
        if (products == null || products.isEmpty()) {
            log.info("No ranking data found for date: {}", yesterday);
            return ApiResponse.success(List.of());
        }
        
        List<RankingEntry> entries = products.stream()
            .map(productKey -> {
                Long productId = Long.parseLong(productKey.replace("product:", ""));
                Double score = redisTemplate.opsForZSet().score(sumKey, productKey);
                return new RankingEntry(productId, score != null ? score : 0.0);
            })
            .collect(Collectors.toList());
            
        log.info("Ranking response - date: {}, results: {}", yesterday, entries.size());
        return ApiResponse.success(entries);
    }
    
    private String generateSumKey(LocalDate date) {
        return "rk:sum:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
