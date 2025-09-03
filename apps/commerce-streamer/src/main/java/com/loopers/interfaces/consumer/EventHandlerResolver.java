package com.loopers.interfaces.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventHandlerResolver {
    
    private final List<EventHandler> handlers;
    
    public EventHandler resolve(String eventType) {
        return handlers.stream()
            .filter(handler -> handler.canHandle(eventType))
            .findFirst()
            .orElse(new DefaultEventHandler());
    }
    
    private static class DefaultEventHandler implements EventHandler {
        @Override
        public boolean canHandle(String eventType) {
            return true;
        }
        
        @Override
        public void handle(EventEnvelopeProcessor.GeneralEnvelopeEvent envelope) {
            System.out.println("Unhandled event: " + envelope.type());
        }
    }
}
