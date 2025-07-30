package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.model.Brand;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {

    private final JpaBrandRepository jpaBrandRepository;

    @Override
    public Optional<Brand> findById(Long id) {
        return jpaBrandRepository.findById(id)
            .map(BrandEntity::toModel);
    }

    @Override
    public Brand save(Brand brand) {
        return jpaBrandRepository.save(BrandEntity.from(brand))
            .toModel(); // Domain Model → Entity → 다시 Model로
    }
}
