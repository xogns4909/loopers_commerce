package com.loopers.application.cache.keys;

import com.loopers.application.cache.CacheKey;

public final class ProductListKey implements CacheKey {

    private final String ver;
    private final int page;
    private final int size;
    private final String sort;

    public ProductListKey(String ver, int page, int size, String sort) {
        this.ver = ver;
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    @Override
    public String asString() {
        return "shop:prod:list:v" + ver + ":p" + page + ":sz" + size + ":sort:" + sort;
    }
}
