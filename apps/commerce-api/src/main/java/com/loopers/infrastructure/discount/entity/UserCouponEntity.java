package com.loopers.infrastructure.discount.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.discount.UserCoupon;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_coupons")
public class UserCouponEntity extends BaseEntity {

    @Version
    private Long version;
    private String userId;
    private String discountType;
    private BigDecimal discountValue;
    private boolean used;


    public UserCoupon toModel() {
        return UserCoupon.reconstruct(
            getId(),
            userId,
            discountType,
            discountValue,
            used,
            version
        );
    }

    public static UserCouponEntity from(UserCoupon coupon) {
        UserCouponEntity entity = new UserCouponEntity();
        entity.setId(coupon.getId());
        entity.userId = coupon.getUserId();
        entity.discountType = coupon.getPolicy().type().name(); // Enum → String
        entity.discountValue = coupon.getPolicy().value();
        entity.used = coupon.isUsed();
        entity.version = coupon.getVersion();
        return entity;
    }
}
