package com.loopers.domain.discount;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCoupon> findById(Long couponId);

    Optional<UserCoupon> findByIdAndUserId(Long id, String userId);

    UserCoupon save(UserCoupon userCoupon);

}
