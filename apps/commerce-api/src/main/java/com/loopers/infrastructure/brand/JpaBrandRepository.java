package com.loopers.infrastructure.brand;

import com.loopers.infrastructure.brand.Entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBrandRepository extends JpaRepository<BrandEntity,Long> {


}
