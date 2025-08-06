package com.loopers.application.coupon;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.discount.UserCouponRepository;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final UserCouponRepository userCouponRepository;

    @Override
    public BigDecimal apply(UserId userId, Long couponId, BigDecimal orderTotal) {
        UserCoupon coupon = getCouponByUserId(couponId, userId.value());

        if (coupon == null) {
            return orderTotal;
        }
        BigDecimal discountAmount = applyCoupon(coupon, orderTotal);

        userCouponRepository.save(coupon.use());
        return discountAmount;
    }

    @Override
    public UserCoupon getCouponByUserId(Long id, String userId) {
        return userCouponRepository.findByIdAndUserId(id, userId).orElse(null);
    }

    private UserCoupon getCoupon(Long couponId) {
        return userCouponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
    }

    private BigDecimal applyCoupon(UserCoupon coupon, BigDecimal orderTotal) {
        return coupon.apply(orderTotal);
    }
}
