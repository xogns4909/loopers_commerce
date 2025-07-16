package com.loopers.interfaces.api.point;


import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.RegisterUserRequest;
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
class PointQueryE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String REGISTER_URL = "/api/v1/users";
    private static final String POINT_QUERY_URL = "/api/v1/points";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
    void findPoint_success() {
        // given
        RegisterUserRequest request = new RegisterUserRequest(
            "kth4909", "kth@loopers.com", "M", "1999-10-23"
        );
        restTemplate.postForEntity(REGISTER_URL, request, Void.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-USER-ID", "kth4909");
        HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<PointResponse>> response = restTemplate.exchange(
            POINT_QUERY_URL,
            HttpMethod.GET,
            httpRequest,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().userId()).isEqualTo("kth4909");
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
    void findPoint_missingHeader_returns400() {
        // given
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            POINT_QUERY_URL,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
        assertThat(response.getBody().data()).isNull();
    }
}
