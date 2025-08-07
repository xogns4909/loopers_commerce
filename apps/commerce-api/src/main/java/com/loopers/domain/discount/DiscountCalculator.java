package com.loopers.domain.discount;

import com.loopers.domain.order.model.OrderAmount;

import java.math.BigDecimal;
public interface DiscountCalculator {
    BigDecimal calculate(BigDecimal orderTotal, BigDecimal discountValue);
}
