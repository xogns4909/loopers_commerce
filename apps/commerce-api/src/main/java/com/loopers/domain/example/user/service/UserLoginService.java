package com.loopers.domain.example.user.service;

import com.loopers.application.example.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserLoginService {

    public UserResponse login(String userId);

}
