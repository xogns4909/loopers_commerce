package com.loopers.domain.discount;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCoupon {
    private final Long id;
    private final String userId;
    private final DiscountPolicy policy;
    private boolean used;


    public BigDecimal apply(BigDecimal orderTotal) {
        if (used) throw new CoreException(ErrorType.BAD_REQUEST,"이미 사용한 쿠폰입니다.");
        used = true;
        return policy.calculate(orderTotal);
    }

    public static UserCoupon reconstruct(Long id, String userId, String discountType, BigDecimal discountValue, boolean used) {
        DiscountPolicy policy = new DiscountPolicy(
            DiscountType.valueOf(discountType),
            discountValue
        );
        return new UserCoupon(id, userId, policy, used);
    }

    public UserCoupon use() {
        this.used = true;
        return this;
    }
}
