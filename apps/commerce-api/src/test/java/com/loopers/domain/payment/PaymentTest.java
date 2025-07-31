package com.loopers.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Payment 도메인 단위 테스트")
class PaymentTest {

    private final UserId userId = UserId.of("taehoon123");

    private Order createOrder() {
        OrderItem item = new OrderItem(1L, 2, Price.of(BigDecimal.valueOf(1000)));
        Order order = Order.create(userId, List.of(item));
        order.assignId(1L);
        return order;
    }

    @Nested
    @DisplayName("결제 생성")
    class Create {

        @Test
        @DisplayName("정상 결제 생성")
        void create_success() {
            Order order = createOrder();

            Payment payment = Payment.complete(order, PaymentMethod.POINT);



            assertThat(payment.getOrderId()).isEqualTo(order.getId());
            assertThat(payment.getUserId()).isEqualTo(userId);
            assertThat(payment.getAmount().value()).isEqualTo(BigDecimal.valueOf(2000));
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.POINT);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("orderId가 null이면 예외 발생")
        void nullOrderId_throws() {
            Order order = createOrder();
            order.assignId(null);

            assertThatThrownBy(() -> Payment.complete(order, PaymentMethod.POINT))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("주문 ID가 지정되지 않았습니다.");
        }

        @Test
        @DisplayName("결제 수단이 null이면 예외 발생")
        void nullMethod_throws() {
            Order order = createOrder();

            assertThatThrownBy(() -> Payment.complete(order, null))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("결제 수단이 유효하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("결제 실패 처리")
    class Fail {

        @Test
        @DisplayName("fail() 호출 시 상태가 FAILED로 변경된다")
        void fail_success() {
            Order order = createOrder();
            Payment payment = Payment.complete(order, PaymentMethod.POINT);

            payment.fail();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
