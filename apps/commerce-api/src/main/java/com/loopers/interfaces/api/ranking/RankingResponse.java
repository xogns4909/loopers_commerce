package com.loopers.interfaces.api.ranking;

import java.time.LocalDate;
import java.util.List;


public record RankingResponse(
    LocalDate date,
    List<RankingEntry> items,
    String source,
    int currentPage,
    int pageSize,
    int totalItems
) {
    
    public static RankingResponse of(LocalDate date, List<RankingEntry> items, 
                                   String source, int page, int size) {
        return new RankingResponse(date, items, source, page, size, items.size());
    }
    
    public static RankingResponse empty(LocalDate date) {
        return new RankingResponse(date, List.of(), "empty", 1, 20, 0);
    }
}
