package com.loopers.infrastructure.discount;

import com.loopers.domain.discount.UserCoupon;
import com.loopers.domain.discount.UserCouponRepository;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.discount.entity.UserCouponEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final JpaUserCouponRepository jpaUserCouponRepository;

    @Override
    public Optional<UserCoupon> findById(Long couponId) {
        return jpaUserCouponRepository.findById(couponId).map(UserCouponEntity::toModel);
    }

    @Override
    public Optional<UserCoupon> findByIdAndUserId(Long id, String userId) {
        return jpaUserCouponRepository.findByIdAndUserId(id,userId).map(UserCouponEntity::toModel);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return jpaUserCouponRepository.save(UserCouponEntity.from(userCoupon)).toModel();
    }

}

