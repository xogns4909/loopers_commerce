package com.loopers.domain.order;

import com.loopers.domain.order.model.Order;

public interface OrderRepository {

    Order save(Order order);
}
