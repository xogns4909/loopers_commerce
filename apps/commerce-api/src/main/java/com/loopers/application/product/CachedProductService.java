package com.loopers.application.product;

import com.loopers.application.cache.*;
import com.loopers.application.cache.PageView.PageView;
import com.loopers.application.cache.keys.ProductDetailKey;
import com.loopers.application.cache.keys.ProductListKey;
import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class CachedProductService implements ProductService {

    private static final String LIST_NS = "shop:ver:prod:list";

    private final @Qualifier("dbProductService") ProductService delegate;
    private final CacheStore<ProductInfo> detailStore;
    private final CacheStore<PageView<ProductInfo>> listStore;
    private final ReadThroughCache rtc;
    private final CachePolicy policy;
    private final VersionClock clock;

    @Override
    public Page<ProductInfo> getProducts(ProductSearchCommand command) {
        if (!isCacheableList(command)) {
            return delegate.getProducts(command);
        }
        
        String ver = clock.current(LIST_NS);
        String sort = normalizeSort(command);
        int page = command.pageable().getPageNumber();
        int size = command.pageable().getPageSize();

        ProductListKey key = new ProductListKey(ver, page, size, sort);

        PageView<ProductInfo> view = rtc.getOrLoad(
            listStore, key, policy.ttlList(),
            () -> {
                Page<ProductInfo> p = delegate.getProducts(command);
                return new PageView<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements());
            },
            policy.ttlNull()
        );

        return new PageImpl<>(view.content, PageRequest.of(view.page, view.size), view.total);
    }

    @Override
    public ProductInfo getProduct(Long productId) {
        return rtc.getOrLoad(
            detailStore, new ProductDetailKey(productId), policy.ttlDetail(),
            () -> {
                try { 
                    return delegate.getProduct(productId); 
                } catch (CoreException e) {
                    if (e.getErrorType() == ErrorType.NOT_FOUND) return null;
                    throw e;
                }
            },
            policy.ttlNull()
        );
    }

    @Override
    @HandleConcurrency
    @Transactional
    public void checkAndDeduct(List<OrderItemCommand> items) {
        delegate.checkAndDeduct(items);
        
        int changed = 0;
        for (OrderItemCommand item : items) {
            detailStore.evict(new ProductDetailKey(item.productId()));
            changed++;
        }
        if (changed >= 20) {
            clock.bump(LIST_NS);
        }
    }

    @Override
    public boolean existsProduct(Long productId) {
        return delegate.existsProduct(productId);
    }

    private boolean isCacheableList(ProductSearchCommand c) {
        return c.pageable().getPageNumber() <= 2 && isHotSort(c);
    }
    
    private boolean isHotSort(ProductSearchCommand c) {
        String s = c.sortType().column();
        return s.contains("createdAt") || s.contains("price") || s.contains("likes");
    }
    
    private String normalizeSort(ProductSearchCommand c) {
        String s = c.sortType().column();
        if (s.contains("price")) return "price";
        if (s.contains("like")) return "like";
        return "createdAt";
    }
}
