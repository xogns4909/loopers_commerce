package com.loopers.service;

import com.loopers.entity.EventMetric;
import com.loopers.repository.EventMetricRepository;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.event.EventTypes;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final EventMetricRepository eventMetricRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Transactional
    public void recordMetric(GeneralEnvelopeEvent envelope) {
        String eventType = envelope.type();
        LocalDateTime now = LocalDateTime.now();
        String metricDate = now.format(DATE_FORMATTER);
        Integer metricHour = now.getHour();
        
        try {

            switch (eventType) {
                case EventTypes.PRODUCT_VIEWED:
                    Long productIdViewed = extractProductId(envelope.payload());
                    if (productIdViewed != null) {
                        incrementMetric(eventType, "view_count", metricDate, metricHour, productIdViewed, BigDecimal.ONE);
                    }
                    break;
                    
                case EventTypes.PRODUCT_LIKED:
                    Long productIdLiked = extractProductId(envelope.payload());
                    if (productIdLiked != null) {
                        incrementMetric(eventType, "like_count", metricDate, metricHour, productIdLiked, BigDecimal.ONE);
                    }
                    break;
                    
                case EventTypes.PRODUCT_UNLIKED:
                    Long productIdUnliked = extractProductId(envelope.payload());
                    if (productIdUnliked != null) {
                        incrementMetric(eventType, "unlike_count", metricDate, metricHour, productIdUnliked, BigDecimal.ONE);
                    }
                    break;
                    
                case EventTypes.ORDER_CREATED:
                    // Order는 여러 Product를 포함할 수 있으므로 각각 처리
                    processOrderCreatedEvent(envelope.payload(), metricDate, metricHour);
                    break;
                    
                default:
                    // 알 수 없는 이벤트는 무시
                    log.debug("Unknown event type for metrics: {}", eventType);
                    break;
            }
            
            log.debug("Metric recorded - type: {}, date: {}, hour: {}", 
                    eventType, metricDate, metricHour);
                    
        } catch (DataAccessException e) {
            // 메트릭 실패는 중요하지 않으므로 로그만 남기고 계속
            log.warn("Failed to record metric for event: {} - {}", 
                    envelope.messageId(), e.getMessage());
        }
    }
    
    private void incrementMetric(String eventType, String metricName, 
                                 String metricDate, Integer metricHour, 
                                 Long productId, BigDecimal incrementValue) {


        EventMetric metric = eventMetricRepository
            .findMetricByKey(eventType, metricName, metricDate, metricHour, productId)
            .orElseGet(() -> EventMetric.builder()
                .eventType(eventType)
                .metricName(metricName)
                .metricDate(metricDate)
                .metricHour(metricHour)
                .productId(productId)
                .metricValue(BigDecimal.ZERO)
                .build());
        
        metric.incrementValue(incrementValue);
        eventMetricRepository.save(metric);
    }

    private Long extractProductId(JsonNode payload) {
        if (payload == null || !payload.has("productId")) {
            log.warn("payload에 productId가 없습니다: {}", payload);
            return null;
        }
        return payload.get("productId").asLong();
    }

    private void processOrderCreatedEvent(JsonNode payload, String metricDate, Integer metricHour) {
        if (payload == null || !payload.has("items")) {
            log.warn("OrderCreated payload에 items가 없습니다: {}", payload);
            return;
        }
        
        JsonNode items = payload.get("items");
        if (items.isArray()) {
            for (JsonNode item : items) {
                if (item.has("productId")) {
                    Long productId = item.get("productId").asLong();
                    incrementMetric(EventTypes.ORDER_CREATED, "order_count", 
                                  metricDate, metricHour, productId, BigDecimal.ONE);
                }
            }
        }
    }
}
