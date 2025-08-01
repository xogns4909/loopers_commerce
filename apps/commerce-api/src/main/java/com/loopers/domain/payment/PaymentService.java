package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.model.UserId;

public interface PaymentService {

    void pay(PaymentCommand paymentCommand);
}
