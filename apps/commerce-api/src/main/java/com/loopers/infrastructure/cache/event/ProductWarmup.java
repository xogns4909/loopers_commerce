package com.loopers.infrastructure.cache.event;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.infrastructure.cache.core.CachePolicy;
import com.loopers.infrastructure.cache.util.PageView;
import com.loopers.infrastructure.cache.core.CacheService;
import com.loopers.infrastructure.cache.strategy.CacheWarmer;
import com.loopers.infrastructure.cache.core.TypeRef;
import com.loopers.infrastructure.cache.keygen.ProductCacheKeyGenerator;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductWarmup implements CacheWarmer {

    private final CacheService cache;
    private final ProductRepository repo;
    private final CachePolicy policy;
    private final ProductCacheKeyGenerator keyGenerator;

    private static final TypeRef<PageView<ProductInfo>> PAGE_OF_PRODUCT = new TypeRef<>() {
    };
    private static final TypeRef<ProductInfo> PRODUCT = new TypeRef<>() {
    };

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmOnBoot() {

        ProductSearchCommand latest = new ProductSearchCommand(
            PageRequest.of(0, 20),
            ProductSortType.LATEST,
            null
        );
        cache.preload(keyGenerator.createListKey(latest), PAGE_OF_PRODUCT,
            () -> PageView.from(repo.searchByCondition(latest)), policy);

        ProductSearchCommand popular = new ProductSearchCommand(
            PageRequest.of(0, 20),
            ProductSortType.LIKES_DESC,
            null
        );
        cache.preload(keyGenerator.createListKey(popular), PAGE_OF_PRODUCT,
            () -> PageView.from(repo.searchByCondition(popular)), policy);

        repo.searchByCondition(
                new ProductSearchCommand(
                    PageRequest.of(0, 30),
                    ProductSortType.LIKES_DESC,
                    null
                )
            ).getContent().stream()
            .map(ProductInfo::productId)
            .forEach(id ->
                cache.preload(keyGenerator.createDetailKey(id), PRODUCT,
                    () -> repo.findProductInfoById(id).orElse(null), policy)
            );
    }

}
