package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.model.PaymentMethod;

public interface PaymentStrategy {
    boolean supports(PaymentMethod method);
    void pay(PaymentCommand command);
}
