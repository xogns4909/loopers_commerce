package com.loopers.domain.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentStrategyFactory strategyFactory;

    @Override
    public void pay(PaymentCommand command) {
        log.info("결제 시작 - orderId: {}, method: {}, amount: {}", 
            command.orderId(), command.paymentMethod(), command.amount());

        strategyFactory
            .getStrategy(command.paymentMethod())
            .pay(command);
    }
}
