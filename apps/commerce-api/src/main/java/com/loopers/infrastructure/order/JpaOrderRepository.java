package com.loopers.infrastructure.order;

import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

}
