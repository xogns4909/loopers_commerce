package com.loopers.domain.example.user;

import org.springframework.stereotype.Component;

@Component
public interface UserRegisterService {

    public UserResponse register(RegisterUserCommand registerUserCommand);

}
