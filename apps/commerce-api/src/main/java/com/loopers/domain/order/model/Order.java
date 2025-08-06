package com.loopers.domain.order.model;

import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {

    private final Long id;
    private final UserId userId;
    private final List<OrderItem> items;
    private final OrderAmount amount;
    private  OrderStatus status;

    private Order(Long id, UserId userId, List<OrderItem> items, OrderAmount amount, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.items = List.copyOf(items);
        this.amount = amount;
        this.status = status;
    }

    public static Order create(UserId userId, List<OrderItem> items, UserCoupon coupon) {
        validate(userId, items);

        OrderAmount originalAmount = OrderAmount.from(items);
        OrderAmount finalAmount = (coupon != null)
            ? OrderAmount.of(coupon.apply(originalAmount.value()))
            : originalAmount;

        return new Order(null, userId, items, finalAmount, OrderStatus.PENDING);
    }

    public static Order reconstruct(Long id, UserId userId, List<OrderItem> items, OrderStatus status, OrderAmount amount) {
        return new Order(id, userId, items, amount, status);
    }

    public void complete() {
        if (status.isFinal()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 완료된 주문입니다.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public Order fail() {
        if (status.isFinal()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 실패 또는 완료된 주문입니다.");
        }
        this.status = OrderStatus.FAILED;
        return this;
    }

    private static void validate(UserId userId, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID가 유효하지 않습니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }
    }


}
