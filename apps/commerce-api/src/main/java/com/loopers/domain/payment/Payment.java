package com.loopers.domain.payment;

import com.loopers.domain.order.model.Order;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

@Getter
public class Payment {
    private final Long orderId;
    private final UserId userId;
    private final PaymentAmount amount;
    private final PaymentMethod method;
    private PaymentStatus status;

    private Payment(Long orderId, UserId userId, PaymentAmount amount, PaymentMethod method) {
        validationValue(orderId, userId, amount, method);

        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.SUCCESS;
    }

    private static void validationValue(Long orderId, UserId userId, PaymentAmount amount, PaymentMethod method) {
        if (orderId == null || orderId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 주문 ID입니다.");
        }
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 ID가 유효하지 않습니다.");
        }
        if (amount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액이 유효하지 않습니다.");
        }
        if (method == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 수단이 유효하지 않습니다.");
        }
    }

    public static Payment complete(Order order, PaymentMethod method) {
        if (order.getId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID가 지정되지 않았습니다.");
        }
        return new Payment(order.getId(), order.getUserId(), PaymentAmount.from(order), method);
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
