package com.loopers.domain.payment.model;


import com.loopers.domain.common.Money;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import java.math.BigDecimal;

public class PaymentAmount extends Money {
    public PaymentAmount(BigDecimal amount) {
        super(amount);
    }

    @Override
    public BigDecimal value() {
        return super.value();
    }

    public static PaymentAmount from(OrderAmount amount) {
        return new PaymentAmount(amount.value());
    }
}
