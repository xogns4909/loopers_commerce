package com.loopers.domain.example;


import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserEntityTest {

    @Test
    @DisplayName("User가 정상적으로 생성된다.")
    void createUser_success() {
        // given
        UserEntity user = UserEntity.builder()
            .userId("kth4909")
            .email("xogns4949@naver.com")
            .name("김태훈")
            .birthDate(LocalDate.of(1999, 10, 23))
            .build();

        // when & then
        Assertions.assertEquals("kth4909", user.getUserId());
        Assertions.assertEquals("xogns4949@naver.com", user.getEmail());
        Assertions.assertEquals("김태훈", user.getName());
        Assertions.assertEquals(LocalDate.of(1999, 10, 23), user.getBirthDate());
    }

}
