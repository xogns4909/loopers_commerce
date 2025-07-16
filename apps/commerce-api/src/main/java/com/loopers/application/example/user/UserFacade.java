package com.loopers.application.example.user;

import com.loopers.domain.example.user.model.User;
import com.loopers.domain.example.user.service.UserFindService;
import com.loopers.domain.example.user.service.UserLoginService;
import com.loopers.interfaces.api.user.LoginRequest;
import com.loopers.interfaces.api.user.RegisterUserRequest;
import com.loopers.domain.example.user.service.UserRegisterService;
import com.loopers.interfaces.api.user.UserInfoRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserRegisterService userRegisterService;

    private final UserLoginService userLoginService;

    private final UserFindService userFindService;

    public UserResponse register(RegisterUserRequest userRequest) {
        return userRegisterService.register(userRequest.toCommand());
    }

    public UserResponse login(LoginRequest loginRequest) {
        return userLoginService.login(loginRequest.userId());
    }

    public UserResponse getUserInfo(UserInfoRequest request) {
        return Optional.ofNullable(userFindService.findByUserId(request.userId()))
            .map(UserResponse::from)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저입니다."));
    }
}
