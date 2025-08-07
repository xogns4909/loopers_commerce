package com.loopers.domain.order;

import com.loopers.domain.order.model.OrderRequestHistory;
import java.util.Optional;

public interface OrderRequestHistoryRepository {

    void save(OrderRequestHistory history);

    Optional<OrderRequestHistory> findByIdempotencyKey(String idempotencyKey);
}
