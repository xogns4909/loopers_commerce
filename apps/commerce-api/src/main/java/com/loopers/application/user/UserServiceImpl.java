package com.loopers.application.user;

import com.loopers.domain.user.model.User;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.user.repository.UserRepository;
import com.loopers.domain.user.service.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    public UserResponse login(String userId) {
        User user = findByUserId(userId);

        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "해당 유저가 존재하지 않습니다.");
        }

        return UserResponse.from(user);
    }

    @Override
    public UserResponse register(RegisterUserCommand registerUserCommand) {

        User user = registerUserCommand.toUserEntity();
        validationDuplicateId(user.getUserId());

        return UserResponse.from(userRepository.save(user));
    }

    private void validationDuplicateId(UserId userId) {

        if (userRepository.existsByUserId(userId.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST,"이미 존재하는 ID 입니다.");
        }

    }
}
