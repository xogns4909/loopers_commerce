package com.loopers.interfaces.api.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.application.product.ProductInfo;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.product.JPAProductRepository;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import java.util.List;
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
class ProductListE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private JPAProductRepository productRepository;

    @Autowired
    private JpaBrandRepository brandRepository;

    private static final String PRODUCT_LIST_URL = "/api/v1/products";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("상품 목록을 가격 내림차순으로 조회하면, 비싼 순서대로 정렬된 결과를 반환한다.")
    void getProductList_sortedByPriceDesc_success() {
        // given
        BrandEntity brand = brandRepository.save(new BrandEntity("무신사"));
        productRepository.save(new ProductEntity("가방", 5000, brand.getId()));
        productRepository.save(new ProductEntity("신발", 15000, brand.getId()));
        productRepository.save(new ProductEntity("자켓", 10000, brand.getId()));

        String url = PRODUCT_LIST_URL + "?page=0&size=10&sortBy=PRICE_DESC";

        // when
        ResponseEntity<ApiResponse<ProductListResponse>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<ProductListResponse> body = response.getBody();

        assertThat(body).isNotNull();
        List<ProductInfo> content = body.data().content();

        assertThat(content).hasSize(3);
        assertThat(content.get(0).price()).isEqualTo(15000);
        assertThat(content.get(1).price()).isEqualTo(10000);
        assertThat(content.get(2).price()).isEqualTo(5000);
    }
}
