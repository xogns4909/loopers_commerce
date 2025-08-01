package com.loopers.domain.order.model;

import com.loopers.domain.product.model.Price;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class OrderItem {
    private final Long productId;
    private final int quantity;
    private final Price unitPrice;

    public OrderItem(Long productId, int quantity, Price unitPrice) {
        validationValue(productId, quantity, unitPrice);
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    private static void validationValue(Long productId, int quantity, Price unitPrice) {
        if (productId == null || productId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 상품 ID입니다.");
        }
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
        if (unitPrice == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격이 유효하지 않습니다.");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.value().multiply(BigDecimal.valueOf(quantity));
    }
}
