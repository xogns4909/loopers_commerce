package com.loopers.domain.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.application.order.OrderDetailCommand;
import com.loopers.application.order.OrderSearchCommand;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {

    Order createOrder(UserId userId, List<OrderItemCommand> items);

    Order save(Order order);

    Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command);

    OrderDetailResponse getOrderDetail(OrderDetailCommand command);

    void completeOrder(Order order);
}
