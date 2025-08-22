package com.loopers.application.order;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompensationService 테스트")
class CompensationServiceTest {

    @Mock private ProductService productService;
    @Mock private CouponService couponService;
    @Mock private OrderService orderService;

    @InjectMocks
    private CompensationService compensationService;

    @Test
    @DisplayName("주문 ID로 재고와 쿠폰을 복원한다")
    void shouldRestoreStockAndCouponByOrderId() {
        // given
        Long orderId = 123L;
        Long productId1 = 1L;
        Long productId2 = 2L;

        UserId userId = UserId.of("testUser");
        Long couponId = 1L;

        var items = List.of(
            new OrderItem(productId1, 2, Price.of(new BigDecimal("10000"))),
            new OrderItem(productId2, 1, Price.of(new BigDecimal("20000")))
        );

        Order orderParam = Order.reconstruct(
            orderId, userId, items, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("30000")), couponId
        );

        Order orderFromDb = mock(Order.class);
        when(orderFromDb.getItems()).thenReturn(items);
        when(orderService.getOrder(orderId)).thenReturn(orderFromDb);

        // when
        compensationService.reverseFor(orderParam);

        // then
        verify(orderService).getOrder(orderId);
        verify(productService).restoreStock(eq(productId1), eq(2));
        verify(productService).restoreStock(eq(productId2), eq(1));
        verify(couponService).releaseSpecificCoupon(couponId, userId.value());
    }

    @Test
    @DisplayName("주문 아이템이 없는 경우에도 쿠폰 해제는 시도한다")
    void shouldAttemptCouponReleaseEvenWithEmptyOrderItems() {
        // given
        Long orderId = 789L;
        UserId userId = UserId.of("testUser");
        Long couponId = 1L;

        var dummyItems = List.of(new OrderItem(1L, 1, Price.of(new BigDecimal("1000")))); // 파라미터용
        Order orderParam = Order.reconstruct(
            orderId, userId, dummyItems, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("1000")), couponId
        );

        Order orderFromDb = mock(Order.class);
        when(orderFromDb.getItems()).thenReturn(List.of()); // 빈 아이템
        when(orderService.getOrder(orderId)).thenReturn(orderFromDb);

        // when
        compensationService.reverseFor(orderParam);

        // then
        verify(orderService).getOrder(orderId);
        verifyNoInteractions(productService); // 재고 복원 없음
        verify(couponService).releaseSpecificCoupon(couponId, userId.value());
    }
}
