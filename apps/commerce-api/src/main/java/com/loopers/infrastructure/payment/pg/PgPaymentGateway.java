package com.loopers.infrastructure.payment.pg;

import com.loopers.infrastructure.payment.pg.dto.PgApiResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentStatusResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentGateway {

    private final PgClient pgClient;

    @Retry(name = "pg-client")
    @Bulkhead(name = "pg-client")
    @CircuitBreaker(name = "pg-client", fallbackMethod = "requestPaymentFallback")
    public PgPaymentResponse requestPayment(String userId, PgPaymentRequest req) {
        PgApiResponse<PgPaymentResponse> res = pgClient.requestPayment(userId, req);
        

        PgPaymentResponse data = res.getValidatedData();
        

        if (data.transactionKey() == null || data.transactionKey().isBlank()) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "거래키 없음");
        }

        return data;
    }

    @Retry(name = "pg-client")
    @CircuitBreaker(name = "pg-client", fallbackMethod = "getPaymentStatusFallback")
    public PgPaymentStatusResponse getPaymentStatus(String userId, String transactionKey) {
        return pgClient.getPaymentStatus(userId, transactionKey);
    }

    @Retry(name = "pg-client")
    @CircuitBreaker(name = "pg-client", fallbackMethod = "getPaymentByOrderIdFallback")
    public PgPaymentStatusResponse getPaymentByOrderId(String userId, String orderId) {
        return pgClient.getPaymentByOrderId(userId, orderId);
    }

    private PgPaymentResponse requestPaymentFallback(String userId, PgPaymentRequest req, Throwable ex) {
        log.error("PG 결제 요청 Fallback 실행 - userId: {}, orderId: {}, error: {}",
            userId, req.orderId(), ex.getMessage());
        throw new CoreException(ErrorType.INTERNAL_ERROR);
    }

    private PgPaymentStatusResponse getPaymentStatusFallback(String userId, String transactionKey, Throwable ex) {
        log.error("PG 상태 조회 Fallback 실행 - userId: {}, transactionKey: {}, error: {}",
            userId, transactionKey, ex.getMessage());
        throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 상태 조회 실패(CB/Retry 이후): ");
    }

    private PgPaymentStatusResponse getPaymentByOrderIdFallback(String userId, String orderId, Throwable ex) {
        log.error("PG 주문 조회 Fallback 실행 - userId: {}, orderId: {}, error: {}",
            userId, orderId, ex.getMessage());
        throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 상태 조회 실패(CB/Retry 이후): ");
    }
}
