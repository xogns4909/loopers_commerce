package com.loopers.domain.example.user;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.loopers.application.example.user.RegisterUserCommand;
import com.loopers.domain.example.user.model.User;
import com.loopers.application.example.user.UserRegisterServiceImpl;
import com.loopers.domain.example.user.repository.UserRepository;
import com.loopers.application.example.user.UserResponse;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserRegisterServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserRegisterServiceImpl userRegisterService;



    @Test
    @DisplayName("중복된 ID 로 가입 시 예외가 발생 된다.")
    void validation_duplicate_Id() {
        //given
        String id = "kth4909";
        RegisterUserCommand cmd = new RegisterUserCommand(id, "kth4909@adfa.com", "M", "1999-10-23");
        given(repository.existsByUserId(id)).willReturn(true);

        //when && then
        thenThrownBy(() -> userRegisterService.register(cmd))
            .isInstanceOf(CoreException.class)
            .hasMessage("이미 존재하는 ID 입니다.");
    }


    @Test
    @DisplayName("성공적으로 가입이 된다.")
    void register_success() {

        // given
        RegisterUserCommand registerUserCommand = new RegisterUserCommand("kth4909", "kth4909@adfa.com", "M", "1999-10-23");
        User savedEntity = registerUserCommand.toUserEntity();
        given(repository.existsByUserId(savedEntity.getUserId().value())).willReturn(false);
        given(repository.save(any(User.class))).willReturn(savedEntity);

        //when
        UserResponse response = userRegisterService.register(registerUserCommand);


        //then
        then(response).isNotNull();
        then(response.userId()).isEqualTo("kth4909");

    }
    

}
