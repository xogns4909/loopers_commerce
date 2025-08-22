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

        List<OrderItem> items = List.of(
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

    @Test
    @DisplayName("쿠폰을 사용하지 않은 주문은 재고만 복원한다")
    void shouldRestoreOnlyStockWhenNoCouponUsed() {
        // given
        Long orderId = 456L;
        Long productId = 10L;
        UserId userId = UserId.of("user123");

        var items = List.of(
            new OrderItem(productId, 3, Price.of(new BigDecimal("5000")))
        );

        Order orderParam = Order.reconstruct(
            orderId, userId, items, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("15000")), null // 쿠폰 없음
        );

        Order orderFromDb = mock(Order.class);
        when(orderFromDb.getItems()).thenReturn(items);
        when(orderService.getOrder(orderId)).thenReturn(orderFromDb);

        // when
        compensationService.reverseFor(orderParam);

        // then
        verify(orderService).getOrder(orderId);
        verify(productService).restoreStock(eq(productId), eq(3));
        verifyNoInteractions(couponService); // 쿠폰 복원 없음
    }

    @Test
    @DisplayName("여러 상품의 재고를 모두 복원한다")
    void shouldRestoreAllProductStocks() {
        // given
        Long orderId = 999L;
        UserId userId = UserId.of("multiUser");
        Long couponId = 5L;

        List<OrderItem> items = List.of(
            new OrderItem(1L, 2, Price.of(new BigDecimal("1000"))),
            new OrderItem(2L, 1, Price.of(new BigDecimal("2000"))),
            new OrderItem(3L, 5, Price.of(new BigDecimal("500"))),
            new OrderItem(4L, 3, Price.of(new BigDecimal("3000")))
        );

        Order orderParam = Order.reconstruct(
            orderId, userId, items, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("15500")), couponId
        );

        Order orderFromDb = mock(Order.class);
        when(orderFromDb.getItems()).thenReturn(items);
        when(orderService.getOrder(orderId)).thenReturn(orderFromDb);

        // when
        compensationService.reverseFor(orderParam);

        // then
        verify(orderService).getOrder(orderId);
        verify(productService).restoreStock(eq(1L), eq(2));
        verify(productService).restoreStock(eq(2L), eq(1));
        verify(productService).restoreStock(eq(3L), eq(5));
        verify(productService).restoreStock(eq(4L), eq(3));
        verify(couponService).releaseSpecificCoupon(couponId, userId.value());
    }

    @Test
    @DisplayName("동일 상품의 여러 주문 아이템도 각각 복원한다")
    void shouldRestoreEachOrderItemSeparately() {
        // given
        Long orderId = 777L;
        UserId userId = UserId.of("kth4909");
        Long couponId = 7L;

        // 같은 상품ID로 여러 아이템 (실제로는 드물지만 테스트용)
        List<OrderItem> items = List.of(
            new OrderItem(1L, 2, Price.of(new BigDecimal("1000"))),
            new OrderItem(1L, 3, Price.of(new BigDecimal("1000"))) // 같은 상품 ID
        );

        Order orderParam = Order.reconstruct(
            orderId, userId, items, OrderStatus.PENDING, OrderAmount.of(new BigDecimal("5000")), couponId
        );

        Order orderFromDb = mock(Order.class);
        when(orderFromDb.getItems()).thenReturn(items);
        when(orderService.getOrder(orderId)).thenReturn(orderFromDb);

        // when
        compensationService.reverseFor(orderParam);

        // then
        verify(orderService).getOrder(orderId);
        verify(productService, times(2)).restoreStock(eq(1L), anyInt()); // 같은 상품에 2번 호출
        verify(productService).restoreStock(eq(1L), eq(2)); // 첫 번째 아이템
        verify(productService).restoreStock(eq(1L), eq(3)); // 두 번째 아이템
        verify(couponService).releaseSpecificCoupon(couponId, userId.value());
    }


}
