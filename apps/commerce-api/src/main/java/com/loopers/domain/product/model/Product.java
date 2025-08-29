package com.loopers.domain.product.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;


public record Product(Long id,String name, String description, Price price, ProductStatus productStatus, Stock stock,Long brandId) {

    public static Product of(Long id,String name, String description, BigDecimal price, ProductStatus status, int quantity,Long brandId) {
        return new Product(
            id,
            name,
            description,
            Price.of(price),
            status,
            Stock.of(quantity),
            brandId
        );
    }

    public boolean isAvailable() {
        return this.productStatus == ProductStatus.AVAILABLE;
    }

    public void checkPurchasable(int quantity) {
        if (!isAvailable()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (!stock.isEnough(quantity)) {
            throw new CoreException(ErrorType.STOCK_SHORTAGE);
        }
    }

    public Product deductStock(int quantity) {
        return new Product(
            this.id,
            this.name,
            this.description,
            this.price,
            this.productStatus,
            this.stock.minus(quantity),
            this.brandId
        );
    }

    public Product restoreStock(int quantity) {
        return new Product(
            this.id,
            this.name,
            this.description,
            this.price,
            this.productStatus,
            this.stock.plus(quantity),
            this.brandId
        );
    }
}
