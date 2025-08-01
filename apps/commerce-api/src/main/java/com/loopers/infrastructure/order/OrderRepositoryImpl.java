package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.model.Order;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.order.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return  jpaOrderRepository.save(OrderEntity.from(order)).toModel();
    }
}
