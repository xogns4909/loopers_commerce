package com.loopers.domain.product.like;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.product.model.Product;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.product.JPAProductRepository;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.infrastructure.product.like.JPAProductLikeRepository;
import com.loopers.infrastructure.product.like.entity.ProductLikeEntity;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
@DisplayName("ProductLikeService 동시성 통합 테스트")
class ProductLikeConcurrencyIntegrationTest {

    @Autowired DatabaseCleanUp databaseCleanUp;
    @Autowired JpaBrandRepository brandRepository;
    @Autowired JPAProductRepository productRepository;
    @Autowired JPAProductLikeRepository productLikeRepository;

    @Autowired ProductLikeService productLikeService;

    private Long productId;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        var brand = brandRepository.save(new BrandEntity("동시성테스트브랜드"));
        var product = ProductEntity.from(
            Product.of(
                null, "동시성 테스트 상품", "설명",
                BigDecimal.valueOf(10000),
                com.loopers.domain.product.model.ProductStatus.AVAILABLE,
                100, brand.getId()
            )
        );
        productId = productRepository.save(product).getId();
        productRepository.flush();

        productLikeRepository.save(new ProductLikeEntity(productId, 0));
        productLikeRepository.flush();

    }

    @Test
    @DisplayName("동시 증가 - 정확히 누적된다")
    void concurrent_increment_like() throws InterruptedException {
        int threads = 100;
        runConcurrent(threads, () -> productLikeService.incrementLike(productId));

        var result = productLikeRepository.findByProductId(productId).orElseThrow();
        assertThat(result.getLikeCount()).isEqualTo(threads);
    }

    @Test
    @DisplayName("동시 감소 - 0 아래로 내려가지 않는다")
    void concurrent_decrement_like_not_below_zero() throws InterruptedException {

        runConcurrent(10, () -> productLikeService.incrementLike(productId));

        int threads = 50;
        runConcurrent(threads, () -> productLikeService.decrementLike(productId));

        var result = productLikeRepository.findByProductId(productId).orElseThrow();
        assertThat(result.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("증가/감소 혼합 - 일관된 결과")
    void concurrent_mixed_operations() throws InterruptedException {

        runConcurrent(50, () -> productLikeService.incrementLike(productId));

        int inc = 30, dec = 20, total = inc + dec;
        ExecutorService pool = Executors.newFixedThreadPool(total);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(total);

        for (int i = 0; i < inc; i++) {
            pool.submit(() -> { await(start); try { productLikeService.incrementLike(productId); } finally { done.countDown(); }});
        }
        for (int i = 0; i < dec; i++) {
            pool.submit(() -> { await(start); try { productLikeService.decrementLike(productId); } finally { done.countDown(); }});
        }

        start.countDown();
        done.await();
        pool.shutdown();

        var result = productLikeRepository.findByProductId(productId).orElseThrow();
        assertThat(result.getLikeCount()).isEqualTo(50 + inc - dec);
    }

    private void runConcurrent(int threads, Runnable task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> { await(start); try { task.run(); } finally { done.countDown(); }});
        }
        start.countDown();
        done.await();
        pool.shutdown();
    }
    private static void await(CountDownLatch l) { try { l.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
