package com.loopers.ranking;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RankingKeyGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    

    public String generateDailyKey(LocalDate date) {
        return "ranking:daily:" + date.format(DATE_FORMATTER);
    }
    

    public String generateProductMember(Long productId) {
        return "product:" + productId;
    }
    

    public LocalDate parseDate(String date) {
        if ("today".equals(date)) {
            return LocalDate.now();
        }
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}
