package com.loopers.interfaces.api.brand;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.brand.Entity.BrandEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrandInfoE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JpaBrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String BRAND_QUERY_URL = "/api/v1/brands";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("브랜드 조회에 성공할 경우, 브랜드 이름을 응답으로 반환한다.")
    void findBrand_success() {
        // given
        BrandEntity saved = brandRepository.save(new BrandEntity("무신사"));

        // when
        ResponseEntity<ApiResponse<BrandResponse>> response = restTemplate.exchange(
            BRAND_QUERY_URL + "/" + saved.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(saved.getId());
        assertThat(response.getBody().data().name()).isEqualTo("무신사");
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 요청할 경우, 404 Not Found 응답을 반환한다.")
    void findBrand_notFound_returns404() {
        // when
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            BRAND_QUERY_URL + "/9999",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
        assertThat(response.getBody().data()).isNull();
    }
}
