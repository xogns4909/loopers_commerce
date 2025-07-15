package com.loopers.domain.example;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.BDDMockito.given;

import com.loopers.domain.example.user.RegisterUserCommand;
import com.loopers.domain.example.user.UserRegisterService;
import com.loopers.domain.example.user.UserRegisterServiceImpl;
import com.loopers.domain.example.user.UserRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserRegisterServiceImpl userService;



    @BeforeEach
    void setUp() {
        userService = new UserRegisterServiceImpl(repository); // ← repository가 null임!
    }

    @Test
    @DisplayName("중복된 ID 로 가입 시 예외가 발생 된다.")
    void validation_duplicate_Id() {
        //given
        String id = "kth4909";
        RegisterUserCommand cmd = new RegisterUserCommand(id, "kth4909@adfa.com", "M", "1999-10-23");
        given(repository.findByUserId(id)).willReturn(true);

        //when && then
        thenThrownBy(() -> userService.register(cmd))
            .isInstanceOf(CoreException.class)
            .hasMessage("이미 존재하는 ID 입니다.");
    }


}
