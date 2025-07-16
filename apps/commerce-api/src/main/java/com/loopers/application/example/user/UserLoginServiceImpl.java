package com.loopers.application.example.user;

import com.loopers.domain.example.user.model.User;
import com.loopers.domain.example.user.repository.UserRepository;
import com.loopers.domain.example.user.service.UserLoginService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserRepository userRepository;

    @Override
    public UserResponse login(String userId) {
        User user = userRepository.findByUserId(userId);

        if(userId == null){
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        return UserResponse.from(user);
    }
}
