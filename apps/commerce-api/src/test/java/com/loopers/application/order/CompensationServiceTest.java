package com.loopers.application.order;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompensationServiceTest {

    @Mock
    private ProductService productService;
    
    @Mock
    private CouponService couponService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CompensationService compensationService;

    @Test
    @DisplayName("주문 ID로 재고와 쿠폰을 복원한다")
    void shouldRestoreStockAndCouponByOrderId() {
        // given
        Long orderId = 123L;
        Long productId1 = 1L;
        Long productId2 = 2L;
        
        List<OrderItem> orderItems = Arrays.asList(
            new OrderItem(productId1, 2, Price.of(BigDecimal.valueOf(10000))),
            new OrderItem(productId2, 1, Price.of(BigDecimal.valueOf(20000)))
        );
        
        Order mockOrder = mock(Order.class);
        when(mockOrder.getItems()).thenReturn(orderItems);
        when(orderService.getOrder(orderId)).thenReturn(mockOrder);

        // when
        compensationService.reverseFor(orderId);

        // then
        // 1. 주문 조회 확인
        verify(orderService).getOrder(orderId);
        
        // 2. 각 상품별 재고 복원 확인
        verify(productService).restoreStock(eq(productId1), eq(2));
        verify(productService).restoreStock(eq(productId2), eq(1));
        
        // 3. 쿠폰 해제 확인
        verify(couponService).releaseByOrderId(orderId);
    }

    @Test
    @DisplayName("쿠폰 해제 실패 시에도 재고 복원은 성공한다")
    void shouldRestoreStockEvenWhenCouponReleaseFails() {
        // given
        Long orderId = 456L;
        Long productId = 1L;
        
        List<OrderItem> orderItems = Arrays.asList(
            new OrderItem(productId, 3, Price.of(BigDecimal.valueOf(15000)))
        );
        
        Order mockOrder = mock(Order.class);
        when(mockOrder.getItems()).thenReturn(orderItems);
        when(orderService.getOrder(orderId)).thenReturn(mockOrder);
        
        // 쿠폰 해제 시 예외 발생 설정
        doThrow(new RuntimeException("쿠폰 해제 실패")).when(couponService).releaseByOrderId(orderId);

        // when
        compensationService.reverseFor(orderId);

        // then
        // 재고 복원은 여전히 수행되어야 함
        verify(productService).restoreStock(eq(productId), eq(3));
        verify(couponService).releaseByOrderId(orderId);
    }

    @Test
    @DisplayName("주문 아이템이 없는 경우에도 쿠폰 해제는 시도한다")
    void shouldAttemptCouponReleaseEvenWithEmptyOrderItems() {
        // given
        Long orderId = 789L;
        
        Order mockOrder = mock(Order.class);
        when(mockOrder.getItems()).thenReturn(Arrays.asList());
        when(orderService.getOrder(orderId)).thenReturn(mockOrder);

        // when
        compensationService.reverseFor(orderId);

        // then
        verify(orderService).getOrder(orderId);
        verify(couponService).releaseByOrderId(orderId);
        // 재고 복원은 호출되지 않아야 함
        verifyNoInteractions(productService);
    }
}
