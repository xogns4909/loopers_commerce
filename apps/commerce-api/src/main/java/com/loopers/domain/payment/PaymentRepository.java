package com.loopers.domain.payment;

import com.loopers.domain.payment.model.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

}
