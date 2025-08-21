package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.PaymentCommand;
import com.loopers.application.payment.PaymentCallbackFacade;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.payment.PaymentDataService;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.strategy.CardPaymentStrategy;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.payment.pg.PgClient;
import com.loopers.infrastructure.payment.pg.dto.PgApiResponse;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.pg.dto.PgPaymentResponse;
import com.loopers.interfaces.api.order.OrderItemRequest;
import com.loopers.interfaces.api.order.OrderRequest;
import com.loopers.interfaces.api.order.OrderResponse;
import com.loopers.interfaces.api.payment.PaymentCallbackRequest;
import feign.FeignException;
import feign.Request;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("CARD 결제 통합 테스트")
class CardPaymentIntegrationTest {

    @Mock
    private PgClient pgClient;
    
    @Mock
    private PaymentDataService paymentDataService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private OrderFacade orderFacade;
    
    @Mock
    private PaymentCallbackFacade paymentCallbackFacade;

    @InjectMocks
    private CardPaymentStrategy cardPaymentStrategy;

    private PaymentCommand paymentCommand;
    private final Long PAYMENT_ID = 1L;
    private final Long ORDER_ID = 100L;
    private final String USER_ID = "testUser";

    @BeforeEach
    void setUp() {
        paymentCommand = new PaymentCommand(
            UserId.of(USER_ID),
            ORDER_ID,
            OrderAmount.of(BigDecimal.valueOf(10000)),
            PaymentMethod.CARD
        );
    }

    @Test
    @DisplayName("카드 결제 전체 성공 시나리오")
    void card_payment_full_success_scenario() {
        // given:
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123456", "SUCCESS", "결제 요청 성공");
        PgApiResponse<PgPaymentResponse> apiResponse = new PgApiResponse<>(
            new PgApiResponse.Meta("SUCCESS", null, "성공"),
            pgResponse
        );
        when(pgClient.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenReturn(apiResponse);

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then
        verify(paymentDataService).createInitiatedPayment(paymentCommand);
        verify(paymentDataService).updateToProcessing(PAYMENT_ID, "tx_123456");
        verify(pgClient).requestPayment(eq(USER_ID), any(PgPaymentRequest.class));
    }

    @Test
    @DisplayName("PG 서버 500 에러 시 실패 이벤트 발행")
    void pg_server_500_error_publishes_failed_event() {
        // given
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        Request mockRequest = Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        when(pgClient.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenThrow(new FeignException.InternalServerError("서버 오류", mockRequest, null, Collections.emptyMap()));

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then:
        verify(paymentDataService).createInitiatedPayment(paymentCommand);
        verify(eventPublisher).publishEvent(any(com.loopers.domain.payment.event.PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("PG 타임아웃 시 실패 이벤트 발행")
    void pg_timeout_publishes_failed_event() {
        // given
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        Request mockRequest = Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        when(pgClient.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenThrow(new FeignException.GatewayTimeout("타임아웃", mockRequest, null, Collections.emptyMap()));

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then
        verify(eventPublisher).publishEvent(any(com.loopers.domain.payment.event.PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("PG 응답에 transactionKey가 없으면 실패 이벤트 발행")
    void pg_response_without_transaction_key_publishes_failed_event() {
        // given: transactionKey가 null인 응답
        when(paymentDataService.createInitiatedPayment(paymentCommand)).thenReturn(PAYMENT_ID);
        
        PgPaymentResponse pgResponse = new PgPaymentResponse(null, "SUCCESS", "결제 요청 성공");
        PgApiResponse<PgPaymentResponse> apiResponse = new PgApiResponse<>(
            new PgApiResponse.Meta("SUCCESS", null, "성공"),
            pgResponse
        );
        when(pgClient.requestPayment(eq(USER_ID), any(PgPaymentRequest.class)))
            .thenReturn(apiResponse);

        // when
        cardPaymentStrategy.pay(paymentCommand);

        // then
        verify(eventPublisher).publishEvent(any(com.loopers.domain.payment.event.PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("여러 결제 요청 동시 처리")
    void multiple_payment_requests_handling() {
        // given
        PaymentCommand command1 = createPaymentCommand("user1", 101L);
        PaymentCommand command2 = createPaymentCommand("user2", 102L);
        PaymentCommand command3 = createPaymentCommand("user3", 103L);

        when(paymentDataService.createInitiatedPayment(any())).thenReturn(1L, 2L, 3L);
        
        PgPaymentResponse pgResponse = new PgPaymentResponse("tx_123", "SUCCESS", "성공");
        PgApiResponse<PgPaymentResponse> apiResponse = new PgApiResponse<>(
            new PgApiResponse.Meta("SUCCESS", null, "성공"),
            pgResponse
        );
        when(pgClient.requestPayment(any(), any())).thenReturn(apiResponse);

        // when
        cardPaymentStrategy.pay(command1);
        cardPaymentStrategy.pay(command2);
        cardPaymentStrategy.pay(command3);

        // then
        verify(paymentDataService, org.mockito.Mockito.times(3)).createInitiatedPayment(any());
        verify(pgClient, org.mockito.Mockito.times(3)).requestPayment(any(), any());
    }

    private PaymentCommand createPaymentCommand(String userId, Long orderId) {
        return new PaymentCommand(
            UserId.of(userId),
            orderId,
            OrderAmount.of(BigDecimal.valueOf(5000)),
            PaymentMethod.CARD
        );
    }
}
