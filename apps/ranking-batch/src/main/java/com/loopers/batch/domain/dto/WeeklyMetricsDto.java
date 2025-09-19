package com.loopers.batch.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WeeklyMetricsDto {
    

    private Long productId;
    

    private LocalDate targetDate;
    

    private LocalDate periodStartDate;
    

    private LocalDate periodEndDate;
    

    private Long viewCount;
    

    private Long likeCount;
    

    private Long orderCount;
}
