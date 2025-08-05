package com.loopers.domain.order.model;

public record OrderRequestHistory(
    Long id,
    String idempotencyKey,
    String userId,
    Long orderId,
    OrderRequestStatus status
) {
    public static OrderRequestHistory of(String key, String userId, Long orderId) {
        return new OrderRequestHistory(null, key, userId, orderId, OrderRequestStatus.PENDING);
    }

    public OrderRequestHistory markSuccess() {
        return new OrderRequestHistory(this.id, this.idempotencyKey, this.userId, this.orderId, OrderRequestStatus.SUCCESS);
    }

    public OrderRequestHistory markFailure() {
        return new OrderRequestHistory(this.id, this.idempotencyKey, this.userId, this.orderId, OrderRequestStatus.FAILURE);
    }
}
