package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.infrastructure.order.entity.OrderItemEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;

    @Override
    public void saveAll(List<OrderItem> items, Long orderId) {
        List<OrderItemEntity> entities = items.stream()
            .map(item -> OrderItemEntity.from(item, orderId))
            .toList();
        jpaOrderItemRepository.saveAll(entities);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return jpaOrderItemRepository.findByOrderId(orderId).stream()
            .map(OrderItemEntity::toModel)
            .toList();
    }
}
