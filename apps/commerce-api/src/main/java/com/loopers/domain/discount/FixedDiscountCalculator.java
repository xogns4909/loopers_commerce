package com.loopers.domain.discount;


import java.math.BigDecimal;

public class FixedDiscountCalculator implements DiscountCalculator {

    @Override
    public BigDecimal calculate(BigDecimal orderTotal, BigDecimal discountValue) {
        BigDecimal discounted = orderTotal.subtract(discountValue);
        return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
    }
}


