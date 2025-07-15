package com.loopers.application.example.user;

import com.loopers.interfaces.api.user.RegisterUserRequest;
import com.loopers.domain.example.user.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserRegisterService userRegisterService;

    public UserResponse register(RegisterUserRequest userRequest) {
        return userRegisterService.register(userRequest.toCommand());
    }

}
