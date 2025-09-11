package com.loopers.ranking;

import static com.loopers.event.EventTypes.ORDER_CREATED;
import static com.loopers.event.EventTypes.PRODUCT_LIKED;
import static com.loopers.event.EventTypes.PRODUCT_UNLIKED;
import static com.loopers.event.EventTypes.PRODUCT_VIEWED;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RankingKeyGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateSignalKey(LocalDate date, String eventType) {



        String suffix = switch(eventType) {
            case PRODUCT_VIEWED -> "view";
            case PRODUCT_LIKED -> "like";
            case PRODUCT_UNLIKED -> "unlike";
            case ORDER_CREATED -> "order";
            default -> "unknown";
        };
        return "rk:sig:" + suffix + ":" + date.format(DATE_FORMATTER);
    }
    
    public String generateSumKey(LocalDate date) {
        return "rk:all:" + date.format(DATE_FORMATTER);
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
