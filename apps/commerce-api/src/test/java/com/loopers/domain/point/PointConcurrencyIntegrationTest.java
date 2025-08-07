package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.application.point.AddPointCommand;
import com.loopers.application.point.PointServiceImpl;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.user.model.User;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.point.JpaPointRepository;
import com.loopers.infrastructure.point.entity.PointEntity;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("PointService 동시성 통합 테스트")
class PointConcurrencyIntegrationTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaPointRepository pointRepository;

    @Autowired
    PointServiceImpl pointService;

    @Autowired
    EntityManager em;

    private final String USER_ID = "kth4909";

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        // 유저 등록
        User user = User.of(USER_ID, "xogns4949@naver.com", "M", "1999-10-23");
        userRepository.save(UserEntity.fromDomain(user));

        // 초기 포인트 등록 (0)
        pointRepository.save(PointEntity.from(Point.reconstruct(null, USER_ID, BigDecimal.ZERO, null)));
        pointRepository.flush();
    }

    @Test
    @DisplayName("동시 충전 - 낙관적 락으로 인해 일부만 성공하고, 포인트는 정확히 누적된다")
    void charge_concurrent_partialSuccess() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.charge(new AddPointCommand(USER_ID, BigDecimal.valueOf(1000)));
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    conflictCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Point saved = pointService.findByUserId(USER_ID);


        assertThat(saved.getBalance().value())
            .isEqualByComparingTo(BigDecimal.valueOf(successCount.get() * 1000L));


        assertThat(successCount.get()).isLessThan(threadCount);
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
    }


    @Test
    @DisplayName("동시 차감 - 최대 5건만 성공하고 나머지는 낙관적 락으로 실패한다")
    void deduct_concurrent_singleUsePerBalance() throws InterruptedException {
        // 사전 충전
        pointService.charge(new AddPointCommand(USER_ID, BigDecimal.valueOf(5000)));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.deduct(
                        UserId.of(USER_ID),
                        PaymentAmount.from(OrderAmount.of(BigDecimal.valueOf(1000)))
                    );
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    conflictCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();


        Point saved = pointService.findByUserId(USER_ID);
        BigDecimal remaining = saved.getBalance().value();

        assertThat(successCount.get()).isLessThanOrEqualTo(5);
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(threadCount);
        assertThat(remaining.compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
    }
}
