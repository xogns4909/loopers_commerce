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
    private Long version;


    public BigDecimal apply(BigDecimal orderTotal) {
        if (used) throw new CoreException(ErrorType.BAD_REQUEST,"이미 사용한 쿠폰입니다.");
        used = true;
        return policy.calculate(orderTotal);
    }

    public static UserCoupon reconstruct(Long id, String userId, String discountType, BigDecimal discountValue, boolean used,Long version) {
        DiscountPolicy policy = new DiscountPolicy(
            DiscountType.valueOf(discountType),
            discountValue
        );
        return new UserCoupon(id, userId, policy, used,version);
    }

    public UserCoupon use() {
        return new UserCoupon(this.id, this.userId, this.policy, true, this.version);
    }
    
    public UserCoupon release() {
        if (!used) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용되지 않은 쿠폰은 해제할 수 없습니다.");
        }
        return new UserCoupon(this.id, this.userId, this.policy, false, this.version);
    }
}
