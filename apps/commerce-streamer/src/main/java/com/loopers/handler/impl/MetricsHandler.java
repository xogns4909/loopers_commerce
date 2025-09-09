package com.loopers.handler.impl;

import com.loopers.event.EventTypes;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import com.loopers.service.MetricsService;
import com.loopers.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsHandler implements EventHandler {

    private final MetricsService metricsService;
    private final RankingService rankingService;

    @Override
    public boolean canHandle(String eventType) {
        boolean canHandle = EventTypes.METRIC_EVENTS.contains(eventType);
        log.debug("MetricsHandler.canHandle({}) = {}", eventType, canHandle);
        return canHandle;
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        log.info("MetricsHandler processing event - type: {}, messageId: {}", 
                envelope.type(), envelope.messageId());
        
        metricsService.recordMetric(envelope);
        rankingService.updateRanking(envelope);
    }
}
