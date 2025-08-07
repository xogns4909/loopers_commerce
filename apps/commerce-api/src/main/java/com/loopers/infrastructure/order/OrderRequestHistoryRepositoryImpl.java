package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderRequestHistoryRepository;
import com.loopers.domain.order.model.OrderRequestHistory;
import com.loopers.infrastructure.order.entity.OrderRequestHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRequestHistoryRepositoryImpl implements OrderRequestHistoryRepository {

    private final JpaOrderRequestHistoryRepository jpaRepository;

    @Override
    public void save(OrderRequestHistory history) {
        jpaRepository.save(OrderRequestHistoryEntity.from(history));
    }

    @Override
    public Optional<OrderRequestHistory> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
            .map(OrderRequestHistoryEntity::toModel);
    }
}
