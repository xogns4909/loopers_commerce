package com.loopers.domain.example;


import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserEntityTest {

    @Test
    @DisplayName("User가 정상적으로 생성된다.")
    void createUser_success() {
        // given
        UserEntity user = UserEntity.builder()
            .userId("kth4909")
            .email("xogns4949@naver.com")
            .name("김태훈")
            .birthDate("1999-10-23")
            .build();

        // when & then
        Assertions.assertEquals("kth4909", user.getUserId());
        Assertions.assertEquals("xogns4949@naver.com", user.getEmail());
        Assertions.assertEquals("김태훈", user.getName());
        Assertions.assertEquals("1999-10-23", user.getBirthDate());
    }

    @DisplayName("유효하지 않은 ID 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"invalidId1!", "toooolongusername", "abc@"})
    void user_invalidId(String invalidId){
        String email = "xogns4949@naver.com";
        String name = "김태훈";
        String birthDate = "1999-10-23";

        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(invalidId, email, name, birthDate);
        });
    }

    @DisplayName("유효하지 않은 메일 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"plainemail", "user@.com", "user@domain", "user@naver..com"})
    void user_invalidEmail(String invalidEmail){
        String id = "xogns4909";
        String name = "김태훈";
        String birthDate = "1999-10-23";


        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(id, invalidEmail, name, birthDate);
        });
    }

    @DisplayName("유효하지 않은 생년월일 포맷이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"19990101", "99-10-23", "10-23-1999", "abcd-ef-gh"})
    void user_invalidBirthDat(String invalidBirthDate) {
        // given
        String id = "xogns4909";
        String email = "xogns4949@naver.com";
        String name = "김태훈";

        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(id, email, name, invalidBirthDate);
        });
    }


}
