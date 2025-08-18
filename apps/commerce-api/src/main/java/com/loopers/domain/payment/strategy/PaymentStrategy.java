package com.loopers.domain.payment.strategy;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.model.PaymentMethod;

import java.util.concurrent.CompletableFuture;

public interface PaymentStrategy {
    boolean supports(PaymentMethod method);
    CompletableFuture<Void> pay(PaymentCommand command);
}
