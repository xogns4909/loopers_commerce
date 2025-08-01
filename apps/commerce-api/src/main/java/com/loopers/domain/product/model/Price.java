package com.loopers.domain.product.model;

import com.loopers.domain.common.Money;
import java.math.BigDecimal;

public class Price extends Money {

    private Price(BigDecimal amount) {
        super(amount);
    }

    public static Price of(BigDecimal amount) {
        return new Price(amount);
    }
}
