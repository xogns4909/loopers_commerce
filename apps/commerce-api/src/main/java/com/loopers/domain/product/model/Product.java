package com.loopers.domain.product.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;


public record Product(Long id,String name, String description, Price price, ProductStatus productStatus, Stock stock) {

    public static Product of(Long id,String name, String description, BigDecimal price, ProductStatus status, int quantity) {
        return new Product(
            id,
            name,
            description,
            Price.of(price),
            status,
            Stock.of(quantity)
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
}
