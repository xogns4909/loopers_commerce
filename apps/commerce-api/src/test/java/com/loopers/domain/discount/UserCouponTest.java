package com.loopers.domain.discount;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserCouponTest {

    @Test
    @DisplayName("정액 쿠폰 적용 시 금액 차감 후 쿠폰 사용 처리됨")
    void applyFixedDiscountAndMarkUsed() {
        UserCoupon coupon = UserCoupon.reconstruct(1L, "user123", "FIXED", BigDecimal.valueOf(1000), false);

        BigDecimal discount = coupon.apply(BigDecimal.valueOf(10000));

        assertThat(discount).isEqualByComparingTo("9000");
        assertThat(coupon.isUsed()).isTrue();
    }

    @Test
    @DisplayName("정률 쿠폰 적용 시 할인율만큼 금액 차감")
    void applyPercentageDiscount() {
        UserCoupon coupon = UserCoupon.reconstruct(2L, "user123", "PERCENT", BigDecimal.valueOf(10), false);

        BigDecimal discount = coupon.apply(BigDecimal.valueOf(10000));

        assertThat(discount).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("이미 사용한 쿠폰은 적용 시도 시 CoreException 예외 발생")
    void throwExceptionWhenCouponAlreadyUsed() {
        UserCoupon coupon = UserCoupon.reconstruct(3L, "user123", "FIXED", BigDecimal.valueOf(1000), true);

        assertThatThrownBy(() -> coupon.apply(BigDecimal.valueOf(10000)))
            .isInstanceOf(CoreException.class)
            .hasMessage("이미 사용한 쿠폰입니다.");
    }
}
