package com.loopers.domain.example;


import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

import com.loopers.application.example.user.UserFindServiceImpl;
import com.loopers.domain.example.user.model.User;
import com.loopers.domain.example.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserFindServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFindServiceImpl userFindService;

    @Test
    @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
    void find_userInfo_Success() {
        //given
        String userId = "kth4909";
        User user = User.of("kth4909", "xogns4949@naver.com", "M", "1999-10-23");

        given(userRepository.findByUserId(userId))
            .willReturn(user);

        // when
        User foundUser = userFindService.findByUserId(userId);

        //then
        then(foundUser).isNotNull();
        then(foundUser.getUserId().value()).isEqualTo(userId);
        then(foundUser.getEmail().value()).isEqualTo("xogns4949@naver.com");
    }


    @Test
    @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
    void find_userInfo_fail() {

        // given
        String nonExistentUserId = "nonExistentUser";

        //when
        given(userRepository.findByUserId(nonExistentUserId))
            .willReturn(null);

        // then
        Assertions.assertThat(userFindService.findByUserId(nonExistentUserId)).isNull();
    }


}
