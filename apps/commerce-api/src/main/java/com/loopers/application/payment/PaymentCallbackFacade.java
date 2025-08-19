package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCallbackService;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentCallbackFacade {

    private final PaymentCallbackService paymentCallbackService;

    @Transactional
    public void processCallback(PaymentCallbackRequest request) {
        paymentCallbackService.processCallback(request);
    }
}
