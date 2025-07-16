package com.loopers.interfaces.api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.application.user.UserResponse;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserInfoE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String REGISTER_URL = "/api/v1/users";
    private static final String USER_INFO_URL = "/api/v1/users/me";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    class FindUserInfo {

        @Test
        @DisplayName("존재하는 유저의 정보를 조회하면 200 OK와 유저 정보를 반환한다.")
        void findUserInfo_success() {
            // given
            RegisterUserRequest request = new RegisterUserRequest(
                "kth4909", "kth@loopers.com", "M", "1999-10-23");

            restTemplate.postForEntity(REGISTER_URL, request, Void.class);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", "kth4909");
            HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

            // when
            ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                httpRequest,
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data().userId()).isEqualTo("kth4909");
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID로 조회하면 404 Not Found를 반환한다.")
        void findUserInfo_fail_notFound() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", "unknownUser");
            HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

            // when
            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                httpRequest,
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
        }
    }
}
