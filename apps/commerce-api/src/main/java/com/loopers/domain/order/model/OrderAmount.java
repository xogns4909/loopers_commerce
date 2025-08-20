package com.loopers.domain.order.model;

import com.loopers.domain.common.Money;
import java.math.BigDecimal;
import java.util.List;
import lombok.ToString;

@ToString
public class OrderAmount extends Money {

    public OrderAmount(BigDecimal amount) {
        super(amount);
    }

    public static OrderAmount from(List<OrderItem> items) {
        BigDecimal total = items.stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderAmount(total);
    }

    public static OrderAmount of(BigDecimal apply) {
        return  new OrderAmount(apply);
    }
}
