package com.loopers.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentDiscountCalculator implements DiscountCalculator {
    @Override
    public BigDecimal calculate(BigDecimal orderTotal, BigDecimal discountValue) {
        BigDecimal rate = discountValue.divide(BigDecimal.valueOf(100));
        return orderTotal.multiply(rate).setScale(0, RoundingMode.DOWN);
    }
}
