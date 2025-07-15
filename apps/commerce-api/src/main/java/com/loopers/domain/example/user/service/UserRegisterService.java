package com.loopers.domain.example.user.service;

import com.loopers.application.example.user.RegisterUserCommand;
import com.loopers.application.example.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserRegisterService {

    public UserResponse register(RegisterUserCommand registerUserCommand);

}
