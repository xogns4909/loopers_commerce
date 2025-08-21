package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentService;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final PaymentService paymentService;

    @Transactional
    public void processCallback(PaymentCallbackRequest request) {
        paymentService.processCallback(request);
    }
}
