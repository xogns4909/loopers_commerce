package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandFacade brandFacade;

    @GetMapping("/{brandId}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrand(
        @PathVariable Long brandId
    ) {
        BrandResponse brand = brandFacade.getBrand(brandId);
        return ResponseEntity.ok(ApiResponse.success(brand));
    }
}
