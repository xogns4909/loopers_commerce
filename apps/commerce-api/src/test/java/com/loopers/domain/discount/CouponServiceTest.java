package com.loopers.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.application.coupon.CouponServiceImpl;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponServiceImplTest {

    private UserCouponRepository userCouponRepository;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        userCouponRepository = mock(UserCouponRepository.class);
        couponService = new CouponServiceImpl(userCouponRepository);
    }

    @Test
    @DisplayName("정상 쿠폰은 할인 적용 후 저장된다")
    void apply_valid_coupon() {
        // given
        Long couponId = 1L;
        UserId userId = UserId.of("kth4909");
        BigDecimal total = BigDecimal.valueOf(10000);
        UserCoupon coupon = UserCoupon.reconstruct(
            couponId, userId.value(), "FIXED", BigDecimal.valueOf(2000), false
        );

        when(userCouponRepository.findByIdAndUserId(couponId, userId.value()))
            .thenReturn(Optional.of(coupon));

        // when
        BigDecimal discount = couponService.apply(userId, couponId, total);

        // then
        assertThat(discount).isEqualByComparingTo("8000");
        verify(userCouponRepository).save(coupon);
    }

    @Test
    @DisplayName("쿠폰이 없으면 할인 없이 원금 그대로 반환")
    void apply_coupon_not_found() {
        // given
        Long couponId = 99L;
        UserId userId = UserId.of("kth4909");
        BigDecimal total = BigDecimal.valueOf(10000);

        when(userCouponRepository.findByIdAndUserId(couponId, userId.value()))
            .thenReturn(Optional.empty());

        // when
        BigDecimal result = couponService.apply(userId, couponId, total);

        // then
        assertThat(result).isEqualByComparingTo("10000");
        verify(userCouponRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 예외가 발생한다")
    void apply_used_coupon_throws() {
        // given
        Long couponId = 2L;
        UserId userId = UserId.of("kth4909");
        BigDecimal total = BigDecimal.valueOf(10000);

        UserCoupon usedCoupon = UserCoupon.reconstruct(
            couponId, userId.value(), "FIXED", BigDecimal.valueOf(3000), true
        );

        when(userCouponRepository.findByIdAndUserId(couponId, userId.value()))
            .thenReturn(Optional.of(usedCoupon));

        // when & then
        assertThatThrownBy(() -> couponService.apply(userId, couponId, total))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("이미 사용한 쿠폰");

        verify(userCouponRepository, never()).save(any());
    }
}
