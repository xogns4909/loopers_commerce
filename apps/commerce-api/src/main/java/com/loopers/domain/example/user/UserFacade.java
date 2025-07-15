package com.loopers.domain.example.user;

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
