package com.loopers.infrastructure.brand.Entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.model.Brand;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "brand")
public class BrandEntity extends BaseEntity {

    private String name;

    public Brand toModel() {
        return new Brand(getId(), this.name);
    }

    public static BrandEntity from(Brand brand){
        return new BrandEntity(brand.name());
    }
}
