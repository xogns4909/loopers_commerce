package com.loopers.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockShortageHandler implements EventHandler {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    @Override
    public boolean canHandle(String eventType) {
        return "STOCK_SHORTAGE".equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        try {
            JsonNode payload = objectMapper.valueToTree(envelope.payload());

            List<String> cacheKeys = extractCacheKeys(payload);

            for (String key : cacheKeys) {
                if (key.contains("*")) {
                    Set<String> matchedKeys = redisTemplate.keys(key); // 일단 keys() 유지
                    if (matchedKeys != null && !matchedKeys.isEmpty()) {
                        redisTemplate.delete(matchedKeys);
                        log.info("Deleted {} keys by pattern: {}", matchedKeys.size(), key);
                    }
                } else {
                    redisTemplate.delete(key);
                    log.info("Deleted key: {}", key);
                }
            }

            long productId = payload.path("productId").asLong();
            int currentStock = payload.path("currentStock").asInt();
            int requestedQuantity = payload.path("requestedQuantity").asInt();

            log.info("Stock shortage handled - productId: {}, current: {}, requested: {}",
                productId, currentStock, requestedQuantity);

        } catch (Exception e) {
            log.error("Failed to handle stock shortage - messageId: {}", envelope.messageId(), e);
        }
    }
    private List<String> extractCacheKeys(JsonNode payload) {
        List<String> keys = new ArrayList<>();
        
        JsonNode cacheKeysNode = payload.get("cacheKeys");
        if (cacheKeysNode != null && cacheKeysNode.isArray()) {
            cacheKeysNode.forEach(node -> keys.add(node.asText()));
        }
        
        return keys;
    }
}
