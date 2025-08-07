package com.loopers.domain.discount;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;


public record DiscountPolicy(DiscountType type, BigDecimal value) {

    public BigDecimal calculate(BigDecimal orderTotal) {
        return switch (type) {
            case FIXED -> new FixedDiscountCalculator().calculate(orderTotal, value);
            case PERCENT -> new PercentDiscountCalculator().calculate(orderTotal, value);
        };
    }
}
