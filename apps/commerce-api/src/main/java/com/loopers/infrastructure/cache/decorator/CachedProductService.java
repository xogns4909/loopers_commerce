package com.loopers.infrastructure.cache.decorator;

import com.loopers.infrastructure.cache.util.PageView;
import com.loopers.infrastructure.cache.core.CacheService;
import com.loopers.infrastructure.cache.core.TypeRef;
import com.loopers.infrastructure.cache.strategy.UpdateType;
import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.infrastructure.cache.core.CachePolicy;
import com.loopers.infrastructure.cache.keygen.ProductCacheKeyGenerator;
import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Product;
import com.loopers.infrastructure.cache.event.ProductEvent;
import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CachedProductService implements ProductService {

    private final ProductRepository repo;
    private final CacheService cache;
    private final CachePolicy policy;
    private final ApplicationEventPublisher publisher;
    private final ProductCacheKeyGenerator keyGenerator;

    private static final TypeRef<ProductInfo> PRODUCT = new TypeRef<>() {};
    private static final TypeRef<PageView<ProductInfo>> PAGE_OF_PRODUCT = new TypeRef<>() {
    };

    @Override
    public ProductInfo getProduct(Long id) {
        return cache.getOrLoad(
            keyGenerator.createDetailKey(id),
            PRODUCT,
            () -> repo.findProductInfoById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND)),
            policy
        );
    }

    @Override
    public Page<ProductInfo> getProducts(ProductSearchCommand c) {
        return cache.getOrLoad(
            keyGenerator.createListKey(c),
            PAGE_OF_PRODUCT,
            () -> {
                Page<ProductInfo> result = repo.searchByCondition(c);
                return PageView.from(result);
            },
            policy
        ).toPage();
    }

    @Override
    @Transactional
    @HandleConcurrency
    public void checkAndDeduct(List<OrderItemCommand> items) {
        for (OrderItemCommand item : items) {
            Product product = repo.findWithPessimisticLockById(item.productId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
            product.checkPurchasable(item.quantity());
            Product deductedProduct = product.deductStock(item.quantity());
            repo.save(deductedProduct);

            publisher.publishEvent(new ProductEvent(item.productId(), UpdateType.STOCK_CHANGED));
        }
    }

    @Override
    @Transactional
    public void restoreStock(Long productId, int quantity) {

        Product product = repo.findWithPessimisticLockById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        Product restoredProduct = product.restoreStock(quantity);
        repo.save(restoredProduct);
        publisher.publishEvent(new ProductEvent(productId, UpdateType.STOCK_CHANGED));

    }

    @Override
    public boolean existsProduct(Long productId) {
        return repo.existsById(productId);
    }
}
