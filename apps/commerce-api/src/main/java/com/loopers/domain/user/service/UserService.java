package com.loopers.domain.user.service;

import com.loopers.application.user.RegisterUserCommand;
import com.loopers.application.user.UserResponse;
import com.loopers.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public interface UserService {

    public User findByUserId(String userId);

    public boolean existsByUserId(String userId);

    public UserResponse login(String userId);

    public UserResponse register(RegisterUserCommand registerUserCommand);


}
