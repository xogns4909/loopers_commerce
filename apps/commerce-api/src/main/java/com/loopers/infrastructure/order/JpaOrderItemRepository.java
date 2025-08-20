package com.loopers.infrastructure.order;

import com.loopers.infrastructure.order.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderItemRepository extends JpaRepository<OrderItemEntity,Long> {

    List<OrderItemEntity> findByOrderId(Long orderId);
}
