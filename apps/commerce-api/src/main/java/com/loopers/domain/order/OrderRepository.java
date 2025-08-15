package com.loopers.domain.order;

import com.loopers.domain.order.model.Order;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderRepository {

    Order save(Order order);

    Page<OrderSummaryResponse> findOrderSummariesByUserId(String userId, Pageable pageable);

    OrderDetailResponse findOrderDetailByUserIdAndOrderId(String userId, Long orderId);

    Optional<Order> findById(Long orderId);
}
