package com.loopers.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.application.coupon.CouponServiceImpl;
import com.loopers.domain.user.model.User;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.discount.JpaUserCouponRepository;
import com.loopers.infrastructure.user.JpaUserRepository;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("CouponService 동시성 통합 테스트")
class CouponConcurrencyIntegrationTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaUserCouponRepository userCouponRepository;

    @Autowired
    CouponServiceImpl couponService;

    @Autowired
    EntityManager em;

    private Long couponId;
    private final String USER_ID = "kth4909";


    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        // 유저 등록
        User user = User.of(USER_ID, "test@naver.com", "M", "1999-10-23");
        userRepository.save(UserEntity.fromDomain(user));


        UserCoupon userCoupon = couponService.saveUserCoupon(new UserCoupon(null, USER_ID, new DiscountPolicy(DiscountType.FIXED, BigDecimal.valueOf(1000)), false, null));
        couponId = userCoupon.getId();
        userCouponRepository.flush();
    }

    @Test
    @DisplayName("동시에 쿠폰 사용 시 1건만 성공하고 나머지는 낙관적 락으로 실패한다")
    void concurrentCouponUse_SingleSuccess() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    couponService.apply(UserId.of(USER_ID), couponId, BigDecimal.valueOf(5000));
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    conflictCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        UserCoupon result = couponService.getCouponByUserId(couponId,USER_ID);


        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(result.isUsed()).isTrue();
    }
}
