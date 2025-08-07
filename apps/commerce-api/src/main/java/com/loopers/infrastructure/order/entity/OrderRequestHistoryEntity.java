package com.loopers.infrastructure.order.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.model.OrderRequestHistory;
import com.loopers.domain.order.model.OrderRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_request_history")
@Getter
@NoArgsConstructor
public class OrderRequestHistoryEntity extends BaseEntity {

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderRequestStatus status;

    public static OrderRequestHistoryEntity from(OrderRequestHistory domain) {
        OrderRequestHistoryEntity entity = new OrderRequestHistoryEntity();
        entity.setId(domain.id());
        entity.idempotencyKey = domain.idempotencyKey();
        entity.userId = domain.userId();
        entity.orderId = domain.orderId();
        entity.status = domain.status();
        return entity;
    }

    public OrderRequestHistory toModel() {
        return new OrderRequestHistory(
            getId(),
            idempotencyKey,
            userId,
            orderId,
            status
        );
    }
}
