package com.loopers.interfaces.api.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.loopers.application.example.user.UserResponse;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
class UserRegisterE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String REGISTER_URL = "/api/v1/users";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    class Register {

        @Test
        @DisplayName("회원 가입이 성공하면 유저 정보를 반환한다.")
        void registerUser_success() {
            // given
            RegisterUserRequest request = new RegisterUserRequest("kth4909", "kth@loopers.com", "M", "1999-10-23");

            // when
            ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                REGISTER_URL,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {
                }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Assertions.assertNotNull(response.getBody());
            assertThat(response.getBody().data().userId()).isEqualTo("kth4909");
        }

        @Test
        @DisplayName("성별이 누락되면 400 BAD_REQUEST 를 반환한다.")
        void registerUser_genderMissing_throws400() {
            // given
            RegisterUserRequest request = new RegisterUserRequest("kth4909", "kth@loopers.com", "", "19991023");

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                REGISTER_URL,
                HttpMethod.POST,
                new HttpEntity<>(request),
                String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
