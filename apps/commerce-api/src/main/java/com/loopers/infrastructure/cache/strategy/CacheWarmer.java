package com.loopers.infrastructure.cache.strategy;


public interface CacheWarmer {
    

    void warmOnBoot();
    

    default void warmAfter(UpdateEvent event) {

    }
}
