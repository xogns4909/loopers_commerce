package com.loopers.interfaces.consumer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.interfaces.consumer.EventHandler;
import com.loopers.interfaces.consumer.EventEnvelopeProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductLikedEventHandler implements EventHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean canHandle(String eventType) {
        return "ProductLiked".equals(eventType);
    }
    
    @Override
    public void handle(EventEnvelopeProcessor.GeneralEnvelopeEvent envelope) {
        ProductLikedEvent event = parsePayload(envelope.payload());
        System.out.println("Product liked: productId=" + event.productId() + ", userId=" + event.userId());
    }
    
    private ProductLikedEvent parsePayload(Object payload) {
        return objectMapper.convertValue(payload, ProductLikedEvent.class);
    }
    
    public record ProductLikedEvent(
        Long productId,
        String userId,
        String context
    ) {}
}
