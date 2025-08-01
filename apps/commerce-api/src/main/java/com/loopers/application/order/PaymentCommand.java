package com.loopers.application.order;

import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;
import lombok.Getter;


public record PaymentCommand(UserId userId, Long orderId, OrderAmount amount, PaymentMethod paymentMethod) {

}

