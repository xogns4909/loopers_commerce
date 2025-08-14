package com.loopers.application.product;


public interface VersionClock {

    String current(String namespace);

    void bump(String namespace);
}

