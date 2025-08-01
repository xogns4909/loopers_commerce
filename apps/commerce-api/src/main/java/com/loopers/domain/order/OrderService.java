package com.loopers.domain.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.user.model.UserId;
import java.util.List;

public interface OrderService {

    Order createOrder(UserId userId, List<OrderItemCommand> items);

    Order save(Order order);
}
