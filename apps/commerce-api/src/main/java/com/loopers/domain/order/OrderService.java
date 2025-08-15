package com.loopers.domain.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.application.order.OrderDetailCommand;
import com.loopers.application.order.OrderSearchCommand;
import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {

    Order save(Order order);

    Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command);

    OrderDetailResponse getOrderDetail(OrderDetailCommand command);


    Order createOrder(UserId userId, List<OrderItemCommand> items, OrderAmount orderAmount);

    void completeOrder(Order order);

    Order getOrder(Long orderId);
}
