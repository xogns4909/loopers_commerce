package com.loopers.application.example.user;

import com.loopers.domain.example.user.service.UserLoginService;
import com.loopers.interfaces.api.user.LoginRequest;
import com.loopers.interfaces.api.user.RegisterUserRequest;
import com.loopers.domain.example.user.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserRegisterService userRegisterService;

    private final UserLoginService userLoginService;

    public UserResponse register(RegisterUserRequest userRequest) {
        return userRegisterService.register(userRequest.toCommand());
    }

    public UserResponse login(LoginRequest loginRequest) {
        return userLoginService.login(loginRequest.userId());
    }
}
