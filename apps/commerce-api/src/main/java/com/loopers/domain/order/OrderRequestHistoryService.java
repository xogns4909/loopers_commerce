package com.loopers.domain.order;

public interface OrderRequestHistoryService {

    void savePending(String idempotencyKey, String userId, Long orderId);

    void markSuccess(String idempotencyKey);

    void markFailure(String idempotencyKey);

}
