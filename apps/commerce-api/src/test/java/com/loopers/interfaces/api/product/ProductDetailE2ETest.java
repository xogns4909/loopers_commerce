package com.loopers.interfaces.api.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.product.JPAProductRepository;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductDetailE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private JPAProductRepository productRepository;

    @Autowired
    private JpaBrandRepository brandRepository;

    private static final String PRODUCT_DETAIL_URL = "/api/v1/products";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("상품 ID로 상세 조회 시, 상품 정보를 응답으로 반환한다.")
    void getProductDetail_success() {
        // given
        BrandEntity brand = brandRepository.save(new BrandEntity("무신사"));

        ProductEntity product = ProductEntity.builder()
            .name("가방")
            .description("가죽 백팩")
            .price(10000)
            .productStatus(com.loopers.domain.product.model.ProductStatus.AVAILABLE)
            .stockQuantity(5)
            .brandId(brand.getId())
            .build();

        ProductEntity saved = productRepository.save(product);

        // when
        String url = PRODUCT_DETAIL_URL + "/" + saved.getId();
        ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        ProductResponse data = response.getBody().data();

        assertThat(data).isNotNull();
        assertThat(data.id()).isEqualTo(saved.getId());
        assertThat(data.name()).isEqualTo("가방");
        assertThat(data.brandName()).isEqualTo("무신사");
        assertThat(data.price()).isEqualTo(10000);
        assertThat(data.likeCount()).isEqualTo(0); // 기본값
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 시, 404 NOT FOUND 응답을 반환한다.")
    void getProductDetail_notFound() {
        // given
        long notExistsProductId = 999999L;
        String url = PRODUCT_DETAIL_URL + "/" + notExistsProductId;

        // when
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );
        System.out.println(response);
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ApiResponse<Object> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.data()).isNull(); // 실패 시 data는 null
        assertThat(body.meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
        assertThat(body.meta().errorCode()).isEqualTo("Not Found");
    }


}
