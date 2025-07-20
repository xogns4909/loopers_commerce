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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserLoginE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String REGISTER_URL = "/api/v1/users";
    private static final String LOGIN_URL = "/api/v1/users/login";


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    class Login {

        @Test
        @DisplayName("존재하는 유저 ID로 로그인하면 X-USER-ID 헤더를 반환한다.")
        void login_success() {
            // given


            RegisterUserRequest registerRequest = new RegisterUserRequest(
                "kth4909", "kth@loopers.com", "M", "1999-10-23");

            restTemplate.postForEntity(REGISTER_URL, registerRequest, Void.class);

            LoginRequest loginRequest = new LoginRequest("kth4909");

            // when
            ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                LOGIN_URL,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("X-USER-ID")).isEqualTo("kth4909");
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data().userId()).isEqualTo("kth4909");
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID로 로그인하면 404를 반환한다.")
        void login_fail_userNotFound() {
            // given
            LoginRequest loginRequest = new LoginRequest("unknownUser");

            // when
            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                LOGIN_URL,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL);
            assertThat(response.getBody().data()).isNull();
        }
    }
}
