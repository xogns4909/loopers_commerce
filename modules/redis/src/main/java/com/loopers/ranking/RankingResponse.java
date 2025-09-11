package com.loopers.ranking;

import java.util.List;

public record RankingResponse(
    List<RankingEntry> rankings,
    long totalCount,
    int currentPage,
    int pageSize
) {}
