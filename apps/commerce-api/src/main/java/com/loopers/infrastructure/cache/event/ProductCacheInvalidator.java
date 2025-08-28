package com.loopers.infrastructure.cache.event;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.infrastructure.cache.strategy.CacheInvalidator;
import com.loopers.infrastructure.cache.core.CacheService;
import com.loopers.infrastructure.cache.keygen.ProductCacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCacheInvalidator implements CacheInvalidator<ProductEvent> {

    private final CacheService cache;
    private final ProductCacheKeyGenerator keyGenerator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductEvent e) {
        switch (e.type()) {
            case DETAIL_CHANGED -> {
                cache.evict(keyGenerator.createDetailKey(e.id()));
            }
            case STOCK_CHANGED, PRICE_CHANGED, LIKE_CHANGED -> {
                cache.evict(keyGenerator.createDetailKey(e.id()));
                cache.bumpNamespace("product");
            }
        }
    }
}
