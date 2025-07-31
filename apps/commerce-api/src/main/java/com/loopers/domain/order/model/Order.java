package com.loopers.domain.order.model;

import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.Getter;

@Getter
public class Order {
    private Long id;
    private final UserId userId;
    private final List<OrderItem> items;
    private final OrderAmount amount;
    private OrderStatus status;

    private Order(UserId userId, List<OrderItem> items) {
        validationValue(userId, items);

        this.userId = userId;
        this.items = List.copyOf(items);
        this.amount = OrderAmount.from(items);
        this.status = OrderStatus.PENDING;
    }

    private static void validationValue(UserId userId, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID가 유효하지 않습니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }
    }

    public static Order create(UserId userId, List<OrderItem> items) {
        return new Order(userId, items);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void complete() {
        if (status.isFinal()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 완료된 주문입니다.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void fail() {
        if (status.isFinal()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 실패 또는 완료된 주문입니다.");
        }
        this.status = OrderStatus.FAILED;
    }
}
