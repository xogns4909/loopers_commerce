package com.loopers.domain.order;


import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.model.Price;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem 단위 테스트")
class OrderItemTest {

    @Nested
    @DisplayName("정상 케이스")
    class ValidCases {

        @ParameterizedTest(name = "productId: {0}, quantity: {1}, price: {2}")
        @CsvSource({
            "1, 1, 1000",
            "99, 100, 99999",
            "10, 1, 0"
        })
        void create_success(Long productId, int quantity, int price) {
            assertThatCode(() ->
                new OrderItem(productId, quantity, Price.of(BigDecimal.valueOf(price)))
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class InvalidCases {

        @ParameterizedTest(name = "productId: {0}")
        @CsvSource({", -1, 0"})
        void invalidProductId_throws(Long productId) {
            assertThatThrownBy(() ->
                new OrderItem(productId, 1, Price.of(BigDecimal.valueOf(1000)))
            )
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("유효하지 않은 상품 ID");
        }

        @ParameterizedTest(name = "quantity: {0}")
        @CsvSource({"0", "-1"})
        void invalidQuantity_throws(int quantity) {
            assertThatThrownBy(() ->
                new OrderItem(1L, quantity,Price.of(BigDecimal.valueOf(1000)))
            )
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("수량은 0보다 커야 합니다");
        }

        @ParameterizedTest(name = "price: {0}")
        @CsvSource({"-1", "-100"})
        void invalidPrice_throws(int price) {
            assertThatThrownBy(() ->
                new OrderItem(1L, 1, Price.of(BigDecimal.valueOf(price)))
            )
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("금액은 0 이상이어야 합니다");
        }

        @DisplayName("unitPrice가 null이면 예외 발생")
        @org.junit.jupiter.api.Test
        void nullPrice_throws() {
            assertThatThrownBy(() -> new OrderItem(1L, 1, null))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("상품 가격이 유효하지 않습니다");
        }
    }
}
