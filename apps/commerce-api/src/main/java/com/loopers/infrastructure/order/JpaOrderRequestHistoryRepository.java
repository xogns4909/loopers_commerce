package com.loopers.infrastructure.order;

import com.loopers.infrastructure.order.entity.OrderRequestHistoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRequestHistoryRepository extends JpaRepository<OrderRequestHistoryEntity,Long> {

    Optional<OrderRequestHistoryEntity> findByIdempotencyKey(String idempotencyKey);

}
