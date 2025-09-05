package com.loopers.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.event.EventTypes;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionHandler implements EventHandler {

    // String 타입으로 통일 (대부분의 캐시 키는 String)
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canHandle(String eventType) {
        boolean canHandle = EventTypes.CACHE_EVICTION_EVENTS.contains(eventType);
        log.debug("CacheEvictionHandler.canHandle({}) = {}", eventType, canHandle);
        return canHandle;
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        String eventType = envelope.type();
        JsonNode payload = objectMapper.valueToTree(envelope.payload());
        
        log.info("CacheEvictionHandler processing event - type: {}, messageId: {}", 
                eventType, envelope.messageId());
        
        switch (eventType) {
            case EventTypes.STOCK_SHORTAGE -> handleStockShortage(payload, envelope.messageId());
            case EventTypes.PRODUCT_UPDATED -> handleProductUpdate(payload, envelope.messageId());
            case EventTypes.PRICE_CHANGED -> handlePriceChange(payload, envelope.messageId());
            case EventTypes.INVENTORY_UPDATED -> handleInventoryUpdate(payload, envelope.messageId());
            default -> log.warn("Unknown event type for cache eviction: {}", eventType);
        }
    }

    private void handleStockShortage(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keyPatterns = Arrays.asList(
            "shop:cache:product:*:id=" + productId + "&type=detail",
            "shop:cache:product:*:*&type=list",
            "shop:cache:ns:product"
        );
        
        deleteKeysWithPattern(keyPatterns, messageId);
        log.warn("Stock shortage cache eviction - productId: {}", productId);
    }

    private void handleProductUpdate(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keyPatterns = Arrays.asList(
            "shop:cache:product:*:id=" + productId + "&type=detail",
            "shop:cache:product:*:*&type=list",
            "shop:cache:ns:product"
        );
        
        deleteKeysWithPattern(keyPatterns, messageId);
        log.info("Product update cache eviction - productId: {}", productId);
    }

    private void handlePriceChange(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keyPatterns = Arrays.asList(
            "shop:cache:product:*:id=" + productId + "&type=detail",
            "shop:cache:product:*:*&type=list",
            "shop:cache:ns:product"
        );
        
        deleteKeysWithPattern(keyPatterns, messageId);
        log.info("Price change cache eviction - productId: {}", productId);
    }

    private void handleInventoryUpdate(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keyPatterns = Arrays.asList(
            "shop:cache:product:*:id=" + productId + "&type=detail",
            "shop:cache:ns:product"
        );
        
        deleteKeysWithPattern(keyPatterns, messageId);
        log.info("Inventory update cache eviction - productId: {}", productId);
    }

    private void deleteKeysWithPattern(List<String> keyPatterns, String messageId) {
        Set<String> keysToDelete = new HashSet<>();
        
        for (String pattern : keyPatterns) {
            Set<String> matchedKeys = redisTemplate.keys(pattern);
            if (matchedKeys != null && !matchedKeys.isEmpty() && matchedKeys.size() <= 200) {
                keysToDelete.addAll(matchedKeys);
            }
        }
        
        if (!keysToDelete.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keysToDelete);
            log.info("Cache eviction completed - messageId: {}, deleted: {} keys", messageId, deletedCount);
        } else {
            log.info("No cache keys found to delete - messageId: {}", messageId);
        }
    }
}
