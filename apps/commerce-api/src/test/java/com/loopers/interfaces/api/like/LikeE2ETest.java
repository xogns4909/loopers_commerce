package com.loopers.interfaces.api.like;

import com.loopers.domain.product.model.Product;
import com.loopers.domain.product.model.ProductStatus;
import com.loopers.domain.user.model.User;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.product.JPAProductRepository;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.infrastructure.user.JpaUserRepository;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LikeE2ETest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaBrandRepository brandRepository;

    @Autowired
    JPAProductRepository productRepository;

    private final String userId = "user100";
    private Long productId;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        // 유저 저장
        User user = User.of(userId, "test@sample.com", "M", "1995-01-01");
        userRepository.save(UserEntity.fromDomain(user));

        // 브랜드 저장
        BrandEntity brand = new BrandEntity("무신사");
        brandRepository.save(brand);

        // 상품 저장 (브랜드 ID 1L 사용)
        Product product = Product.of(
            null,
            "테스트 상품",
            "E2E 테스트용",
            new BigDecimal("12000"),
            ProductStatus.AVAILABLE,
            100,
            1L // brandId
        );
        productId = productRepository.save(ProductEntity.from(product)).getId();
        productRepository.flush();
    }

    @Test
    @DisplayName("좋아요 등록 성공")
    void like_success() {
        ResponseEntity<ApiResponse<LikeResponse>> response = like(productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().liked()).isTrue();
    }

    @Test
    @DisplayName("이미 좋아요한 상품은 ALREADY_LIKED 응답")
    void like_alreadyLiked() {
        like(productId);
        ResponseEntity<ApiResponse<LikeResponse>> response = like(productId);
        assertThat(response.getBody().data().liked()).isTrue();
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void unlike_success() {
        like(productId);
        ResponseEntity<ApiResponse<LikeResponse>> response = unlike(productId);
        assertThat(response.getBody().data().liked()).isFalse();
    }

    @Test
    @DisplayName("좋아요하지 않은 상품은 취소 불가")
    void unlike_notLiked() {
        ResponseEntity<ApiResponse<LikeResponse>> response = unlike(productId);
        assertThat(response.getBody().data().liked()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 상품 좋아요 시도 시 404 반환")
    void like_notFound() {
        ResponseEntity<ApiResponse<LikeResponse>> response = like(99999L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ApiResponse<LikeResponse>> like(Long pid) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"userId\": \"%s\", \"productId\": %d}", userId, pid);

        return restTemplate.exchange(
            "/api/v1/like/products",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {}
        );
    }

    private ResponseEntity<ApiResponse<LikeResponse>> unlike(Long pid) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"userId\": \"%s\", \"productId\": %d}", userId, pid);

        return restTemplate.exchange(
            "/api/v1/like/products",
            HttpMethod.DELETE,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {}
        );
    }
}
