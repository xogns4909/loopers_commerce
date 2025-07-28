package com.loopers.domain.product.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;


public record Product(String name, String description, Price price, ProductStatus productStatus, Stock stock) {

    public static Product of(String name, String description, BigDecimal price, ProductStatus status, int quantity) {
        return new Product(
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
