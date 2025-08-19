package com.loopers.domain.payment.strategy;

import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final List<PaymentStrategy> paymentStrategies;

    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        return paymentStrategies.stream()
            .filter(strategy -> strategy.supports(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new CoreException(
                ErrorType.BAD_REQUEST, 
                "지원하지 않는 결제 방법입니다: " + paymentMethod
            ));
    }
}
