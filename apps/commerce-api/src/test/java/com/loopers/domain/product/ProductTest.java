package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import com.loopers.domain.product.model.Product;
import com.loopers.domain.product.model.ProductStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    @Test
    @DisplayName("상품을 정상적으로 생성할 수 있다")
    void create_success() {
        // given
        String name = "상품명";
        String description = "상품 설명";
        BigDecimal price = BigDecimal.valueOf(5000);
        int quantity = 10;

        // when
        Product product = Product.of(name, description, price, ProductStatus.AVAILABLE, quantity);

        // then
        assertThat(product).isNotNull();
        assertThat(product.name()).isEqualTo(name);
        assertThat(product.description()).isEqualTo(description);
        assertThat(product.price().getAmount()).isEqualByComparingTo(price);
        assertThat(product.stock().getQuantity()).isEqualTo(quantity);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    @DisplayName("음수 가격으로 상품을 생성하면 예외가 발생한다")
    void invalid_price(int invalidPrice) {
        // when & then
        assertThatThrownBy(() ->
            Product.of("상품명", "상품 설명", BigDecimal.valueOf(invalidPrice), ProductStatus.AVAILABLE, 10)
        ).isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreEx = (CoreException) ex;
                assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            });
    }

    @Test
    @DisplayName("판매 불가 상품이면 구매 시 예외가 발생한다")
    void  invalid_stock() {
        // given
        Product product = Product.of("상품명", "상품 설명", BigDecimal.valueOf(5000), ProductStatus.UNAVAILABLE, 10);

        // when & then
        assertThatThrownBy(() -> product.checkPurchasable(1))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreEx = (CoreException) ex;
                assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            });
    }

    @Test
    @DisplayName("재고가 부족하면 구매 시 예외가 발생한다")
    void invalid_status() {
        // given
        Product product = Product.of("상품명", "상품 설명", BigDecimal.valueOf(5000), ProductStatus.AVAILABLE, 1);

        // when & then
        assertThatThrownBy(() -> product.checkPurchasable(10))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreEx = (CoreException) ex;
                assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.STOCK_SHORTAGE);
            });
    }
}
