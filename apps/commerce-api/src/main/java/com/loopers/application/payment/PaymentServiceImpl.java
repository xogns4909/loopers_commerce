package com.loopers.application.payment;

import com.loopers.application.order.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.model.Payment;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.payment.strategy.PaymentStrategy;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final List<PaymentStrategy> strategies;

    @Override
    public void pay(PaymentCommand command) {

        PaymentStrategy strategy = strategies.stream()
            .filter(s -> s.supports(command.paymentMethod()))
            .findFirst()
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 결제 수단입니다."));

        strategy.pay(command);

    }


}
