package com.loopers.application.coupon;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.discount.UserCouponRepository;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final UserCouponRepository userCouponRepository;

    @Override
    @HandleConcurrency(message = "쿠폰 사용 요청에 실패했습니다. 다시 시도해주세요")
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
    @Transactional
    public void releaseSpecificCoupon(Long couponId, String userId) {
        releaseCoupon(couponId, userId);


    }

    @Transactional
    public void releaseCoupon(Long couponId, String userId) {

        UserCoupon coupon = userCouponRepository.findByIdAndUserId(couponId, userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        UserCoupon releasedCoupon = coupon.release();
        userCouponRepository.save(releasedCoupon);

    }

    public UserCoupon saveUserCoupon(UserCoupon userCoupon) {
        return userCouponRepository.save(userCoupon);
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
