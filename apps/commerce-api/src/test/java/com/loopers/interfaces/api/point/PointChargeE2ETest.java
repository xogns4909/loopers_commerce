package com.loopers.interfaces.api.point;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.RegisterUserRequest;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointChargeE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String REGISTER_URL = "/api/v1/users";
    private static final String POINT_CHARGE_URL = "/api/v1/points/charge";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
    void chargePoint_success() {
        // given
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "kth4909", "kth@loopers.com", "M", "1999-10-23"
        );
        restTemplate.postForEntity(REGISTER_URL, registerRequest, Void.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-USER-ID", "kth4909");

        PointChargeRequest chargeRequest = new PointChargeRequest("kth4909", BigDecimal.valueOf(1000));
        HttpEntity<PointChargeRequest> httpRequest = new HttpEntity<>(chargeRequest, headers);

        // when
        ResponseEntity<ApiResponse<PointResponse>> response = restTemplate.exchange(
            POINT_CHARGE_URL,
            HttpMethod.POST,
            httpRequest,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().userId()).isEqualTo("kth4909");
        assertThat(response.getBody().data().balance().intValue()).isEqualTo(1000);
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
    void chargePoint_missingHeader_returns400() {
        // given
        PointChargeRequest chargeRequest = new PointChargeRequest("kth4909", BigDecimal.valueOf(1000));
        HttpHeaders headers = new HttpHeaders(); // intentionally empty
        HttpEntity<PointChargeRequest> httpRequest = new HttpEntity<>(chargeRequest, headers);

        // when
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            POINT_CHARGE_URL,
            HttpMethod.POST,
            httpRequest,
            new ParameterizedTypeReference<>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
        assertThat(response.getBody().data()).isNull();
    }
}
