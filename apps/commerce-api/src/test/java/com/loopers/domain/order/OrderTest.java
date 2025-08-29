package com.loopers.domain.order;

import com.loopers.domain.discount.DiscountPolicy;
import com.loopers.domain.discount.DiscountType;
import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 도메인 단위 테스트")
class OrderTest {

    private final OrderItem item = new OrderItem(1L, 1, Price.of(BigDecimal.valueOf(1000)));

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @Test
        @DisplayName("주문 정상 생성")
        void create_success() {
            UserId userId = UserId.of("user1");

            assertThatCode(() -> Order.create(userId, List.of(item), null,null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("complete() 호출 시 상태가 COMPLETED로 변경됨")
        void complete_success() {
            Order order = Order.create(UserId.of("user1"), List.of(item), null,null);

            order.complete();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("fail() 호출 시 상태가 FAILED로 변경됨")
        void fail_success() {
            Order order = Order.create(UserId.of("user1"), List.of(item), null,null);

            order.fail();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        }

    }

    @Nested
    @DisplayName("예외 케이스")
    class InvalidCases {

        @Test
        @DisplayName("userId가 null이면 예외 발생")
        void nullUserId_throws() {
            assertThatThrownBy(() -> Order.create(null, List.of(item), null,null))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("유저 ID가 유효하지 않습니다");
        }

        @Test
        @DisplayName("아이템 리스트가 비어있으면 예외 발생")
        void emptyItems_throws() {
            assertThatThrownBy(() -> Order.create(UserId.of("user1"), List.of(), null,null))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("주문 항목이 비어있습니다");
        }

        @Test
        @DisplayName("이미 완료된 주문에 complete() 호출 시 예외 발생")
        void complete_twice_throws() {
            Order order = Order.create(UserId.of("user1"), List.of(item), null,null);
            order.complete();

            assertThatThrownBy(order::complete)
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("이미 완료된 주문입니다");
        }

        @Test
        @DisplayName("이미 실패된 주문에 fail() 호출 시 예외 발생")
        void fail_twice_throws() {
            Order order = Order.create(UserId.of("user1"), List.of(item), null,null);
            order.fail();

            assertThatThrownBy(order::fail)
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("이미 실패 또는 완료된 주문입니다");
        }
    }
}
