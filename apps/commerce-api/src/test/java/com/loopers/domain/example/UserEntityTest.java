package com.loopers.domain.example;


import com.loopers.domain.example.user.UserEntity;
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
            .gender("M")
            .birthDay("1999-10-23")
            .build();

        // when & then
        Assertions.assertEquals("kth4909", user.getUserId());
        Assertions.assertEquals("xogns4949@naver.com", user.getEmail());
        Assertions.assertEquals("M", user.getGender());
        Assertions.assertEquals("1999-10-23", user.getBirthDay());
    }

    @DisplayName("유효하지 않은 ID 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"invalidId1!", "toooolongusername", "abc@"})
    void user_invalidId(String invalidId){
        String email = "xogns4949@naver.com";
        String gender = "M";
        String birthDay = "1999-10-23";

        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(invalidId, email, gender, birthDay);
        });
    }

    @DisplayName("유효하지 않은 메일 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"plainemail", "user@.com", "user@domain", "user@naver..com"})
    void user_invalidEmail(String invalidEmail){
        String id = "xogns4909";
        String name = "김태훈";
        String birthDay = "1999-10-23";


        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(id, invalidEmail, name, birthDay);
        });
    }

    @DisplayName("유효하지 않은 생년월일 포맷이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"19990101", "99-10-23", "10-23-1999", "abcd-ef-gh"})
    void user_invalidBirthDat(String invalidBirthDate) {
        // given
        String id = "xogns4909";
        String email = "xogns4949@naver.com";
        String gender = "M";

        // when & then
        Assertions.assertThrows(CoreException.class, () -> {
            new UserEntity(id, email, gender, invalidBirthDate);
        });
    }


}
