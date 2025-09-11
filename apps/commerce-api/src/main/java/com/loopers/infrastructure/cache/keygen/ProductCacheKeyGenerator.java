package com.loopers.infrastructure.cache.keygen;

import com.loopers.infrastructure.cache.core.CacheKey;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.application.product.VersionClock;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductCacheKeyGenerator extends CacheKeyGenerator {

    public ProductCacheKeyGenerator(VersionClock versionClock) {
        super(versionClock);
    }

    @Override
    protected String getNamespace() {
        return "product";
    }

    @Override
    protected String getPrefix() {
        return "shop:cache";
    }

    public CacheKey createDetailKey(Long id) {
        return buildKey(Map.of("type", "detail", "id", id));
    }

    public CacheKey createListKey(ProductSearchCommand cmd) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "list");
        if (cmd.brandId() != null) params.put("brand", cmd.brandId());
        if (cmd.sortType() != null) params.put("sort", cmd.sortType());
        if (cmd.pageable() != null) params.put("page", cmd.pageable().getPageNumber());

        return buildKey(params);
    }
    
    public CacheKey createBatchKey(List<Long> productIds) {

        List<Long> sortedIds = productIds.stream().sorted().toList();
        return buildKey(Map.of("type", "batch", "ids", sortedIds));
    }
    

    public List<String> generateEvictionKeys(Long productId) {
        long version = Long.parseLong(versionClock.current(getNamespace()));
        

        String detailKey = createDetailKey(productId).toString();
        

        String listKeyPattern = String.format("%s:%s:v%d:list:*", getPrefix(), getNamespace(), version);
        

        String stockKey = String.format("%s:%s:v%d:stock:%d", getPrefix(), getNamespace(), version, productId);
        
        return List.of(detailKey, listKeyPattern, stockKey);
    }
}
