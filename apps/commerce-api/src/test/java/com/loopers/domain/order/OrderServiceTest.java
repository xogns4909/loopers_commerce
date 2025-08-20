package com.loopers.domain.order;

import com.loopers.application.order.OrderDetailCommand;
import com.loopers.application.order.OrderSearchCommand;
import com.loopers.application.order.OrderServiceImpl;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderItemDetail;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("사용자 주문 목록을 정상 조회한다")
    void getUserOrders_success() {
        // given
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        OrderSearchCommand command = new OrderSearchCommand(userId, pageable);

        List<OrderSummaryResponse> data = List.of(
            new OrderSummaryResponse(1L, 10000L, OrderStatus.PENDING, ZonedDateTime.now())
        );

        given(orderRepository.findOrderSummariesByUserId(eq(userId), eq(pageable)))
            .willReturn(new PageImpl<>(data, pageable, 1));


        // when
        Page<OrderSummaryResponse> result = orderService.getUserOrders(command);

        System.out.println(result);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).orderId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 상세 정보를 정상 조회한다")
    void getOrderDetail_success() {
        // given
        String userId = "user1";
        Long orderId = 10L;
        OrderDetailCommand command = new OrderDetailCommand(userId, orderId);

        List<OrderItemDetail> items = List.of(
            new OrderItemDetail(100L, "상품A", 2, 5000L)
        );

        OrderDetailResponse response = new OrderDetailResponse(
            orderId,
           10000L,
            OrderStatus.PENDING,
            items
        );

        given(orderRepository.findOrderDetailByUserIdAndOrderId(userId, orderId))
            .willReturn(response);

        // when
        OrderDetailResponse result = orderService.getOrderDetail(command);

        // then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productName()).isEqualTo("상품A");
    }
}
