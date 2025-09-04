package com.loopers.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@ConditionalOnBean(RedisTemplate.class)
public class CacheEvictionHandler implements EventHandler {

    // String 타입으로 통일 (대부분의 캐시 키는 String)
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canHandle(String eventType) {
        // 캐시 무효화가 필요한 이벤트들
        return Arrays.asList(
            "STOCK_SHORTAGE", 
            "PRODUCT_UPDATED", 
            "PRICE_CHANGED",
            "INVENTORY_UPDATED"
        ).contains(eventType.toUpperCase());
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        try {
            String eventType = envelope.type().toUpperCase();
            JsonNode payload = objectMapper.valueToTree(envelope.payload());
            
            switch (eventType) {
                case "STOCK_SHORTAGE" -> handleStockShortage(payload, envelope.messageId());
                case "PRODUCT_UPDATED" -> handleProductUpdate(payload, envelope.messageId());
                case "PRICE_CHANGED" -> handlePriceChange(payload, envelope.messageId());
                case "INVENTORY_UPDATED" -> handleInventoryUpdate(payload, envelope.messageId());
                default -> log.warn("Unknown event type for cache eviction: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process cache eviction - messageId: {}, eventType: {}", 
                     envelope.messageId(), envelope.type(), e);
        }
    }

    private void handleStockShortage(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        int currentStock = payload.path("currentStock").asInt();
        int requestedQty = payload.path("requestedQuantity").asInt();
        

        List<String> keysToDelete = Arrays.asList(
            "product:stock:" + productId,           // 재고 캐시
            "product:availability:" + productId,    // 구매 가능 여부
            "catalog:featured:products",            // 추천 상품 목록  
            "product:details:" + productId          // 상품 상세 정보
        );
        
        deleteSpecificKeys(keysToDelete, messageId);
        
        log.warn("Stock shortage cache eviction - productId: {}, current: {}, requested: {}", 
                productId, currentStock, requestedQty);
    }

    private void handleProductUpdate(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keysToDelete = Arrays.asList(
            "product:details:" + productId,
            "product:summary:" + productId,
            "catalog:category:" + payload.path("categoryId").asText(),
            "search:products:*"
        );
        
        deleteKeysWithPattern(keysToDelete, messageId);
        log.info("Product update cache eviction - productId: {}", productId);
    }

    private void handlePriceChange(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keysToDelete = Arrays.asList(
            "product:price:" + productId,
            "product:details:" + productId,
            "pricing:category:" + payload.path("categoryId").asText()
        );
        
        deleteSpecificKeys(keysToDelete, messageId);
        log.info("Price change cache eviction - productId: {}", productId);
    }

    private void handleInventoryUpdate(JsonNode payload, String messageId) {
        long productId = payload.path("productId").asLong();
        
        List<String> keysToDelete = Arrays.asList(
            "product:stock:" + productId,
            "product:availability:" + productId,
            "inventory:warehouse:" + payload.path("warehouseId").asText()
        );
        
        deleteSpecificKeys(keysToDelete, messageId);
        log.info("Inventory update cache eviction - productId: {}", productId);
    }


    private void deleteSpecificKeys(List<String> keys, String messageId) {
        try {
            Set<String> existingKeys = new HashSet<>();
            
            for (String key : keys) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    existingKeys.add(key);
                }
            }
            
            if (!existingKeys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(existingKeys);
                log.info("Deleted {} specific cache keys - messageId: {}, keys: {}", 
                        deletedCount, messageId, existingKeys);
            } else {
                log.debug("No matching cache keys found - messageId: {}", messageId);
            }
            
        } catch (Exception e) {
            log.error("Failed to delete specific cache keys - messageId: {}", messageId, e);
        }
    }

    /**
     * 패턴이 포함된 키들 처리 (신중하게 사용)
     */
    private void deleteKeysWithPattern(List<String> keysOrPatterns, String messageId) {
        try {
            Set<String> keysToDelete = new HashSet<>();
            
            for (String keyOrPattern : keysOrPatterns) {
                if (keyOrPattern.contains("*")) {
                    // 패턴 매칭은 제한적으로만 사용
                    Set<String> matchedKeys = redisTemplate.keys(keyOrPattern);
                    if (matchedKeys != null && matchedKeys.size() <= 100) { // 안전장치
                        keysToDelete.addAll(matchedKeys);
                    } else {
                        log.warn("Too many keys matched pattern '{}' or null result - skipping deletion", keyOrPattern);
                    }
                } else {
                    // 정확한 키
                    if (Boolean.TRUE.equals(redisTemplate.hasKey(keyOrPattern))) {
                        keysToDelete.add(keyOrPattern);
                    }
                }
            }
            
            if (!keysToDelete.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keysToDelete);
                log.info("Deleted {} cache keys (with patterns) - messageId: {}, count: {}", 
                        deletedCount, messageId, keysToDelete.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to delete cache keys with patterns - messageId: {}", messageId, e);
        }
    }
}
