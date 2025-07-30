package com.loopers.domain.brand;

import com.loopers.domain.brand.model.Brand;
import java.util.Optional;

public interface BrandService {

    Brand findByBrandId(Long id);

}
