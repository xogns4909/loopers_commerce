package com.loopers.domain.user.service;

import com.loopers.application.user.RegisterUserCommand;
import com.loopers.application.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserRegisterService {

    public UserResponse register(RegisterUserCommand registerUserCommand);

}
