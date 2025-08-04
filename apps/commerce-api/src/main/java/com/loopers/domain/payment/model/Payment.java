package com.loopers.domain.payment.model;

import com.loopers.domain.order.model.Order;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

@Getter
public class Payment {

    private final Long id;
    private final Long orderId;
    private final UserId userId;
    private final PaymentAmount amount;
    private final PaymentMethod method;

    private Payment(Long id, Long orderId, UserId userId, PaymentAmount amount, PaymentMethod method) {
        validationValue(orderId, userId, amount, method);
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
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

    public static Payment complete(Long orderId, UserId userId, PaymentAmount amount, PaymentMethod method) {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID가 지정되지 않았습니다.");
        }
        return new Payment(
            null,
            orderId,
            userId,
            amount,
            method
        );
    }

    public static Payment reconstruct(Long id, UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        return new Payment(id, orderId, userId, amount, method);
    }

    public static Payment create(UserId userId, Long orderId, PaymentAmount amount, PaymentMethod method) {
        return new Payment(
            null,
            orderId,
            userId,
            amount,
            method
        );
    }
}
