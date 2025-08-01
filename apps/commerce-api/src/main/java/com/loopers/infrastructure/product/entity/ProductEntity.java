package com.loopers.infrastructure.product.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.product.model.Product;
import com.loopers.domain.product.model.ProductStatus;
import com.loopers.domain.product.model.Stock;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ProductEntity extends BaseEntity {

    private String name;

    private String description;

    private int price;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    private int stockQuantity;

    private Long brandId;

    public Product toModel() {
        return new Product(
            this.getId(),
            this.name,
            this.description,
            Price.of(BigDecimal.valueOf(this.price)),
            this.productStatus,
            Stock.of(this.stockQuantity),
            this.brandId
        );
    }

    public static ProductEntity from(Product product) {
        return new ProductEntity(
            product.name(),
            product.description(),
            product.price().value().intValue(),
            product.productStatus(),
            product.stock().value(),
            product.brandId()
        );
    }
}

