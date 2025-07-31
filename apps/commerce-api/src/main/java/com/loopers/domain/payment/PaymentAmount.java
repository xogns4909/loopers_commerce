package com.loopers.domain.payment;


import com.loopers.domain.common.Money;
import com.loopers.domain.order.model.Order;
import java.math.BigDecimal;

class PaymentAmount extends Money {
    public PaymentAmount(BigDecimal amount) {
        super(amount);
    }

    @Override
    public BigDecimal value() {
        return super.value();
    }

    public static PaymentAmount from(Order order) {
        return new PaymentAmount(order.getAmount().value());
    }
}
