package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCallbackFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCallbackController {

    private final PaymentCallbackFacade paymentCallbackFacade;

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> handleCallback(@RequestBody PaymentCallbackRequest request) {

            paymentCallbackFacade.processCallback(request);
            return ResponseEntity.ok(ApiResponse.success("OK"));

    }
}
