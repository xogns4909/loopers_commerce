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
    public void releaseByOrderId(Long orderId) {
        log.info("쿠폰 해제 시작 - orderId: {}", orderId);
        try {
            // 🔥 현실적인 해결책: 주문-쿠폰 연결 정보가 없는 상황에서의 차선책
            // 
            // 이상적인 해결책:
            // 1. Order 엔티티에 usedCouponId 필드 추가
            // 2. 또는 OrderCoupon 연결 테이블 생성
            // 3. 쿠폰 사용 이력 테이블 생성
            //
            // 현재 상황에서의 차선책:
            // - CompensationService에서 Order 정보와 함께 쿠폰 정보를 전달받는 방식
            // - 또는 최근 사용된 쿠폰 중에서 추정하여 해제 (위험함)
            
            log.warn("쿠폰 해제 로직 제한적 구현 - orderId: {} " +
                     "(완전한 구현을 위해서는 주문-쿠폰 연결 스키마 개선 필요)", orderId);
                     

            
            log.info("쿠폰 해제 완료 (제한적) - orderId: {}", orderId);
        } catch (Exception e) {
            log.error("쿠폰 해제 실패 - orderId: {}, error: {}", orderId, e.getMessage(), e);

        }
    }
    

    @Transactional
    public void releaseCoupon(Long couponId, String userId) {

        UserCoupon coupon = userCouponRepository.findByIdAndUserId(couponId, userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        
        UserCoupon releasedCoupon = coupon.release();
        userCouponRepository.save(releasedCoupon);

    }

    public UserCoupon saveUserCoupon(UserCoupon userCoupon){
        return  userCouponRepository.save(userCoupon);
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
