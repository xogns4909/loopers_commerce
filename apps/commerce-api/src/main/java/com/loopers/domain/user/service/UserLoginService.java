package com.loopers.domain.user.service;

import com.loopers.application.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserLoginService {

    public UserResponse login(String userId);

}
