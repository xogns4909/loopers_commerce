package com.loopers.application.example.user;

import com.loopers.domain.example.user.model.User;
import com.loopers.domain.example.user.repository.UserRepository;
import com.loopers.domain.example.user.service.UserFindService;
import com.loopers.domain.example.user.service.UserLoginService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserFindService userFindService;

    @Override
    public UserResponse login(String userId) {
        User user = userFindService.findByUserId(userId);

        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "해당 유저가 존재하지 않습니다.");
        }

        return UserResponse.from(user);
    }
}
