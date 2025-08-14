package com.loopers.application.cache.keys;


import com.loopers.application.cache.CacheKey;

public final class ProductDetailKey implements CacheKey {

    private final long productId;

    public ProductDetailKey(long productId) {
        this.productId = productId;
    }

    @Override
    public String asString() {
        return "shop:prod:detail:" + productId;
    }
}
