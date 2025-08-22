package com.loopers.domain.product.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

@Getter
public class Stock {

    private final int quantity;

    private Stock(int quantity) {
        if (quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public static Stock of(int quantity) {
        return new Stock(quantity);
    }

    public boolean isEnough(int amount) {
        return this.quantity >= amount;
    }

    public int value() {
        return this.quantity;
    }

    public Stock minus(int quantity) {
        if (quantity < 0 || this.quantity < quantity) {
            throw new CoreException(ErrorType.STOCK_SHORTAGE, "차감할 수 없는 재고입니다.");
        }
        return new Stock(this.quantity - quantity);
    }

    public Stock plus(int quantity) {
        if (quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "복원할 재고는 0 이상이어야 합니다.");
        }
        return new Stock(this.quantity + quantity);
    }
}
