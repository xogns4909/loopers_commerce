package com.loopers.handler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionHandler implements EventHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean canHandle(String eventType) {
        // 이제 STOCK_SHORTAGE 이벤트만 처리
        return "STOCK_SHORTAGE".equalsIgnoreCase(eventType);
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        try {
            JsonNode payload = objectMapper.valueToTree(envelope.payload());

            JsonNode cacheKeysNode = payload.path("cacheKeys");
            if (cacheKeysNode.isArray()) {
                Set<String> deleted = new HashSet<>();
                for (JsonNode node : cacheKeysNode) {
                    String keyOrPattern = node.asText();
                    if (keyOrPattern.contains("*")) {
                        Set<String> keys = redisTemplate.keys(keyOrPattern);
                        if (keys != null && !keys.isEmpty()) {
                            redisTemplate.delete(keys);
                            deleted.addAll(keys);
                        }
                    } else {
                        redisTemplate.delete(keyOrPattern);
                        deleted.add(keyOrPattern);
                    }
                }
                log.info("Deleted {} cache keys for stock shortage: {}", deleted.size(), deleted);
            }

            long productId = payload.path("productId").asLong();
            int currentStock = payload.path("currentStock").asInt();
            int requestedQty = payload.path("requestedQuantity").asInt();

            log.warn("Stock shortage cache eviction - productId={}, current={}, requested={}",
                productId, currentStock, requestedQty);

        } catch (Exception e) {
            log.error("Failed to evict cache for stock shortage - messageId={}", envelope.messageId(), e);
        }
    }
}
