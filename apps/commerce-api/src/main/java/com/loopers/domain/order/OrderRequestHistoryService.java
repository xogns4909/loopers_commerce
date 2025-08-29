package com.loopers.domain.order;

import java.util.Optional;

public interface OrderRequestHistoryService {
    Optional<Long> findOrderIdByIdempotencyKey(String idempotencyKey);


    void saveReceived(String idempotencyKey, String userId, Long orderId);


    void markAccepted(String idempotencyKey);

    void markFailure(String idempotencyKey);
}
