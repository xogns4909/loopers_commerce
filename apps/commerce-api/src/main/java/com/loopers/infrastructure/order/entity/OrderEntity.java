package com.loopers.infrastructure.order.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.user.model.UserId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long amount;
    

    private Long usedCouponId;

    protected OrderEntity() {}

    public static OrderEntity from(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.userId = order.getUserId().value();
        entity.status = order.getStatus();
        entity.amount = order.getAmount().value().longValue();
        entity.usedCouponId = order.getUsedCouponId();
        return entity;
    }

    public Order toModel() {
        return Order.reconstruct(
            getId(),
            UserId.of(userId),
            List.of(),
            status,
            new OrderAmount(BigDecimal.valueOf(amount)),
            usedCouponId
        );
    }
}
