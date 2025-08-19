package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;

public interface PaymentService {
    void pay(PaymentCommand command);
}
