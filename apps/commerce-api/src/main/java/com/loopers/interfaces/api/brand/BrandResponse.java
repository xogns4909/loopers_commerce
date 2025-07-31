package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.model.Brand;

public record BrandResponse(
    Long id,
    String name
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
            brand.id(),
            brand.name()

        );
    }
}
