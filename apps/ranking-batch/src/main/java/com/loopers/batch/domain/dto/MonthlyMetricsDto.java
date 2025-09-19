package com.loopers.batch.domain.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MonthlyMetricsDto {
    private Long productId;
    private LocalDate targetDate;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Long viewCount;
    private Long likeCount;
    private Long orderCount;
}
