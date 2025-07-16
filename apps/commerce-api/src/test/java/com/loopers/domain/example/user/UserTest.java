package com.loopers.domain.example.user;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.loopers.domain.example.user.model.User;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserTest {

    @Test
    @DisplayName("User가 정상적으로 생성된다.")
    void createUser_success() {
        // given
        User user = User.of("kth4909","xogns4949@naver.com","M","1999-10-23");

        // when then
        assertThat(user.toString()).contains("kth4909", "xogns4949@naver.com", "M", "1999-10-23");
    }

    @DisplayName("유효하지 않은 ID 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"invalidId1!", "toooolongusername", "abc@"})
    void invalidUserId(String invalidId) {
        // given
        String email = "xogns4949@naver.com";
        String gender = "M";
        String birthDay = "1999-10-23";

        // when & then
        assertThatThrownBy(() -> User.of(invalidId, email, gender, birthDay))
            .isInstanceOf(CoreException.class)
            .hasMessage("유효하지 않은 아이디입니다.");
        }


    @DisplayName("유효하지 않은 메일 형식이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"plainemail", "user@.com", "user@domain", "user@naver..com"})
    void user_invalidEmail(String invalidEmail){
        // given
        String id = "xogns4909";
        String gender = "김태훈";
        String birthDay = "1999-10-23";


        // when & then
        assertThatThrownBy(() -> User.of(id, invalidEmail, gender, birthDay))
            .isInstanceOf(CoreException.class)
            .hasMessage("유효하지 않은 이메일 형식입니다.");
    }

    @DisplayName("유효하지 않은 생년월일 포맷이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"19990101", "99-10-23", "10-23-1999", "abcd-ef-gh"})
    void user_invalidBirthDay(String invalidBirthDay) {
        // given
        String id = "xogns4909";
        String email = "xogns4949@naver.com";
        String gender = "M";

        // when & then
        assertThatThrownBy(() -> User.of(id, email, gender, invalidBirthDay))
            .isInstanceOf(CoreException.class)
            .hasMessage("유효하지 않은 생년월일 형식입니다.");
    }


}
