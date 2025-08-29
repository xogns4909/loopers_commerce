package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.model.Payment;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;


public interface PaymentService {

    void pay(PaymentCommand command);

    Payment processCallback(PaymentCallbackRequest request);

    Payment findByTransactionKey(String transactionKey);
}
