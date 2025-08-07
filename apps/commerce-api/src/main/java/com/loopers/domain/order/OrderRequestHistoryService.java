package com.loopers.domain.order;

import java.util.Optional;

public interface OrderRequestHistoryService {

    void savePending(String idempotencyKey, String userId, Long orderId);

    void markSuccess(String idempotencyKey);

    void markFailure(String idempotencyKey);

    Optional<Long> findOrderIdByIdempotencyKey(String idempotencyKey);

}
