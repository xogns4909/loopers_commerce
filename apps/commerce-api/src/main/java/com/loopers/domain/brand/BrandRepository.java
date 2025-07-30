package com.loopers.domain.brand;

import com.loopers.domain.brand.model.Brand;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface BrandRepository {

    Optional<Brand> findById(Long id);

    Brand save(Brand brand);
}
