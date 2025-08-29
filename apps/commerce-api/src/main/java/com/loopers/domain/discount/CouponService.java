package com.loopers.domain.discount;

import com.loopers.domain.user.model.UserId;
import java.math.BigDecimal;

public interface CouponService {

    BigDecimal apply(UserId userId, Long couponId, BigDecimal orderTotal);

    UserCoupon getCouponByUserId(Long aLong, String userId);

    void releaseSpecificCoupon(Long couponId, String userId);

}
