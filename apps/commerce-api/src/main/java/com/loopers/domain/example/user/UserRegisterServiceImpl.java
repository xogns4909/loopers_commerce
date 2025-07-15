package com.loopers.domain.example.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final UserRepository userRepository;

    @Override
    public UserResponse register(RegisterUserCommand registerUserCommand) {

        UserEntity user = registerUserCommand.toUserEntity();
        validatioDuplicateId(user.getUserId());

        return null;
    }

    private void validatioDuplicateId(UserId userId) {

        if (userRepository.findByUserId(userId.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 존재하는 ID 입니다.");
        }

    }
}
