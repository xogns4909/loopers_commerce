package com.loopers.domain.payment.model;

import com.loopers.domain.common.Money;
import com.loopers.domain.order.model.OrderAmount;

import java.math.BigDecimal;

public class PaymentAmount extends Money {
    
    public PaymentAmount(BigDecimal amount) {
        super(amount);
    }

    public static PaymentAmount from(OrderAmount orderAmount) {
        return new PaymentAmount(orderAmount.value());
    }

    @Override
    public BigDecimal value() {
        return super.value();
    }
}
