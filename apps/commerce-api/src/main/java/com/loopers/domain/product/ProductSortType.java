package com.loopers.domain.product;

import java.util.Arrays;
public enum ProductSortType {
    PRICE_DESC("price", "desc"),
    LATEST("createdAt", "desc"),
    LIKES_DESC("likes", "desc");

    private final String column;
    private final String direction;

    ProductSortType(String column, String direction) {
        this.column = column;
        this.direction = direction;
    }

    public String column() {
        return column;
    }

    public String direction() {
        return direction;
    }

    public static ProductSortType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(LATEST);
    }
}
