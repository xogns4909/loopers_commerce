package com.loopers.application.user;

import com.loopers.domain.user.service.UserService;
import com.loopers.interfaces.api.user.LoginRequest;
import com.loopers.interfaces.api.user.RegisterUserRequest;
import com.loopers.interfaces.api.user.UserInfoRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {



    private final UserService userService;

    public UserResponse register(RegisterUserRequest userRequest) {
        return userService.register(userRequest.toCommand());
    }

    public UserResponse login(LoginRequest loginRequest) {
        return userService.login(loginRequest.userId());
    }

    public UserResponse getUserInfo(UserInfoRequest request) {
        return Optional.ofNullable(userService.findByUserId(request.userId()))
            .map(UserResponse::from)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저입니다."));
    }
}
