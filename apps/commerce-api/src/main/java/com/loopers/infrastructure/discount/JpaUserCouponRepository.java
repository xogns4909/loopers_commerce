package com.loopers.infrastructure.discount;

import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.discount.entity.UserCouponEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserCouponRepository extends JpaRepository<UserCouponEntity,Long> {

    Optional<UserCouponEntity> findByIdAndUserId(Long id, String userId);
}
