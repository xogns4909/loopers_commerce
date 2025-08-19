package com.loopers.infrastructure.payment.pg;

import com.loopers.infrastructure.payment.pg.dto.PgApiResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "pg-client",
    url = "${pg.base-url}",
    configuration = PgClientConfig.class
)
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PgApiResponse<PgPaymentResponse> requestPayment(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody PgPaymentRequest request
    );


    
    @GetMapping("/api/v1/payments/{transactionKey}")
    PgPaymentStatusResponse getPaymentStatus(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable("transactionKey") String transactionKey
    );
    
    @GetMapping("/api/v1/payments")
    PgPaymentStatusResponse getPaymentByOrderId(
        @RequestHeader("X-USER-ID") String userId,
        @RequestParam("orderId") String orderId
    );
}
