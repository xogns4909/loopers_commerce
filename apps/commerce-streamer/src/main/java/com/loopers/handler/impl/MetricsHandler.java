package com.loopers.handler.impl;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import com.loopers.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsHandler implements EventHandler {

    private final MetricsService metricsService;
    
    // 메트릭을 집계할 이벤트 타입들
    private static final Set<String> METRIC_EVENT_TYPES = Set.of(
        "PRODUCT_VIEWED",
        "PRODUCT_LIKED",
        "PRODUCT_UNLIKED",
        "ORDER_CREATED",
        "PAYMENT_COMPLETED",
        "PAYMENT_FAILED"
    );

    @Override
    public boolean canHandle(String eventType) {
        // 정의된 이벤트 타입만 처리
        return METRIC_EVENT_TYPES.contains(eventType);
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        metricsService.recordMetric(envelope);
    }
}
