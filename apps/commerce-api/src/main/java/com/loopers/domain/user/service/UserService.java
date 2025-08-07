package com.loopers.domain.user.service;

import com.loopers.application.user.RegisterUserCommand;
import com.loopers.application.user.UserResponse;
import com.loopers.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public interface UserService {

    User findByUserId(String userId);

    boolean existsByUserId(String userId);

    UserResponse login(String userId);

    UserResponse register(RegisterUserCommand registerUserCommand);


}
