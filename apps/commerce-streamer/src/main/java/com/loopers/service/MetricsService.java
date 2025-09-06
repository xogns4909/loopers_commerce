package com.loopers.service;

import com.loopers.entity.EventMetric;
import com.loopers.repository.EventMetricRepository;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.event.EventTypes;
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
            // 이벤트 타입별 메트릭 처리
            switch (eventType) {
                case EventTypes.PRODUCT_VIEWED:
                    incrementMetric(eventType, "view_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                case EventTypes.PRODUCT_LIKED:
                    incrementMetric(eventType, "like_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                case EventTypes.PRODUCT_UNLIKED:
                    incrementMetric(eventType, "unlike_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                case EventTypes.ORDER_CREATED:
                    incrementMetric(eventType, "order_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                case EventTypes.PAYMENT_COMPLETED:
                    incrementMetric(eventType, "payment_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                case EventTypes.PAYMENT_FAILED:
                    incrementMetric(eventType, "payment_failed_count", metricDate, metricHour, BigDecimal.ONE);
                    break;
                    
                default:
                    // 모든 이벤트에 대해 기본 카운트 증가
                    incrementMetric(eventType, "event_count", metricDate, metricHour, BigDecimal.ONE);
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
                                 BigDecimal incrementValue) {
        
        // UPSERT 패턴: 있으면 증가, 없으면 생성
        EventMetric metric = eventMetricRepository
            .findByEventTypeAndMetricNameAndMetricDateAndMetricHour(
                eventType, metricName, metricDate, metricHour)
            .orElseGet(() -> EventMetric.builder()
                .eventType(eventType)
                .metricName(metricName)
                .metricDate(metricDate)
                .metricHour(metricHour)
                .metricValue(BigDecimal.ZERO)
                .build());
        
        metric.incrementValue(incrementValue);
        eventMetricRepository.save(metric);
    }
}
