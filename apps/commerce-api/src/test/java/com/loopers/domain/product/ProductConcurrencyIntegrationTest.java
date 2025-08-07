package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.*;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.product.model.Price;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.product.JPAProductRepository;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("ProductService.checkAndDeduct 동시성 통합 테스트")
class ProductConcurrencyIntegrationTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    JpaBrandRepository brandRepository;

    @Autowired
    JPAProductRepository productRepository;

    @Autowired
    ProductService productService;

    @Autowired
    EntityManager em;

    private Long productId;
    private final int initialStock = 10;
    private final int threadCount   = initialStock + 5;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        BrandEntity brand = brandRepository.save(new BrandEntity("동시성테스트브랜드"));
        ProductEntity product = ProductEntity.from(
            com.loopers.domain.product.model.Product.of(
                null,
                "동시성 테스트 상품",
                "설명",
                BigDecimal.valueOf(10000),
                com.loopers.domain.product.model.ProductStatus.AVAILABLE,
                initialStock,
                brand.getId()
            )
        );
        productId = productRepository.save(product).getId();
        productRepository.flush();
    }

    @Test
    @DisplayName("여러 스레드가 동시 차감 시 재고가 음수로 떨어지지 않고, 충돌 건수가 발생한다")
    void concurrency_stock_Test() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);

        AtomicInteger successCount  = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        Runnable task = () -> {
            readyLatch.countDown();
            try {
                startLatch.await();


                productService.checkAndDeduct(
                    Collections.singletonList(
                        new OrderItemCommand(
                            productId,
                            1,
                            Price.of(BigDecimal.valueOf(1000)
                            ), "asd",
                            null
                        )
                    )
                );
                successCount.incrementAndGet();
            } catch (CoreException ex) {
                conflictCount.incrementAndGet();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };


        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }


        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        productRepository.flush();

        ProductEntity after = productRepository.findById(productId).orElseThrow();


        assertThat(successCount.get() + conflictCount.get())
            .isEqualTo(threadCount);


        assertThat(after.getStockQuantity())
            .isEqualTo(initialStock - successCount.get())
            .isGreaterThanOrEqualTo(0);
    }
}
