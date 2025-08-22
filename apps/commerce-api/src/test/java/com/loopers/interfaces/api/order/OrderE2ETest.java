package com.loopers.interfaces.api.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.application.point.AddPointCommand;
import com.loopers.application.point.PointServiceImpl;
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
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderE2ETest {

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

    @Autowired
    PointServiceImpl pointService;

    private final String userId = "testUser1";
    private Long productId;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        // 유저 저장
        User user = User.of(userId, "test@sample.com", "M", "1990-01-01");
        userRepository.save(UserEntity.fromDomain(user));

        pointService.charge(new AddPointCommand(userId,BigDecimal.valueOf(1000000000)));

        BrandEntity brand = new BrandEntity("감성브랜드");
        brandRepository.save(brand);


        Product product = Product.of(
            null,
            "감성 테스트 상품",
            "E2E용 상품",
            BigDecimal.valueOf(10000),
            ProductStatus.AVAILABLE,
            10,
            1L
        );
        productId = productRepository.save(ProductEntity.from(product)).getId();
        productRepository.flush();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void order_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        headers.set("X-IDEMPOTENCY-KEY", "test-idempotency-key-001");
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = """
        {
          "paymentMethod": "POINT",
          "items": [
            {
              "productId": %d,
              "quantity": 2,
              "price": 10000
            }
          ]
        }
        """.formatted(productId);

        HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<ApiResponse<OrderResponse>> response = restTemplate.exchange(
            "/api/v1/orders",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<>() {}
        );

        System.out.println(response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<OrderResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.data().status().name()).isEqualTo("PENDING");
        assertThat(body.data().amount()).isEqualTo(BigDecimal.valueOf(20000));
    }


    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() {
        // given: 주문 먼저 넣기
        ResponseEntity<ApiResponse<OrderResponse>> orderResponse = createOrder();

        Long orderId = orderResponse.getBody().data().orderId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<OrderDetailResponse>> response = restTemplate.exchange(
            "/api/v1/orders/" + orderId,
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OrderDetailResponse detail = response.getBody().data();
        assertThat(detail).isNotNull();
        assertThat(detail.orderId()).isEqualTo(orderId);
        assertThat(detail.items()).hasSize(1);
        assertThat(detail.items().get(0).productName()).isEqualTo("감성 테스트 상품");
    }


    private ResponseEntity<ApiResponse<OrderResponse>> createOrder() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId);
        headers.set("X-IDEMPOTENCY-KEY", "test-key-123");
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = """
        {
          "paymentMethod": "POINT",
          "items": [
            {
              "productId": %d,
              "quantity": 2,
              "price": 10000
            }
          ]
        }
        """.formatted(productId);

        HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(
            "/api/v1/orders",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<>() {}
        );
    }

}
