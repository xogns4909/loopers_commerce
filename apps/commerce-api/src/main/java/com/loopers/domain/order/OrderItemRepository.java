package com.loopers.domain.order;

import com.loopers.domain.order.model.OrderItem;
import java.util.List;

public interface OrderItemRepository {

    void saveAll(List<OrderItem> items, Long id);
    
    List<OrderItem> findByOrderId(Long orderId);
}
