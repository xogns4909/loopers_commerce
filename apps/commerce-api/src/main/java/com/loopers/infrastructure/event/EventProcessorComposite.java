package com.loopers.infrastructure.event;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EventProcessorComposite {
    
    private final List<EventProcessor> processors;

    public EventProcessorComposite(List<EventProcessor> processors) {
        this.processors = processors;
    }

    public void process(String eventTypeStr, Object payload, String messageId, String correlationId) {
        EventType eventType = findEventType(eventTypeStr);
        
        processors.stream()
            .filter(processor -> processor.canProcess(eventType))
            .findFirst()
            .ifPresent(processor -> processor.process(payload, messageId, correlationId));
    }
    
    private EventType findEventType(String eventTypeStr) {
        for (EventType type : EventType.values()) {
            if (type.getValue().equals(eventTypeStr)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + eventTypeStr);
    }
}
