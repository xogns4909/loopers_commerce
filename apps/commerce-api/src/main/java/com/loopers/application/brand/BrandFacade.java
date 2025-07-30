package com.loopers.application.brand;

import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.like.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class BrandFacade {
    private final BrandService brandService;

    public BrandResponse getBrand(Long brandId) {
        return BrandResponse.from(brandService.findByBrandId(brandId));

    }
}
