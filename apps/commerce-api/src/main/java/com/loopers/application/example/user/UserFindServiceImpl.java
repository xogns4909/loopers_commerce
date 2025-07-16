package com.loopers.application.example.user;

import com.loopers.domain.example.user.model.User;
import com.loopers.domain.example.user.repository.UserRepository;
import com.loopers.domain.example.user.service.UserFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFindServiceImpl implements UserFindService {

    private final UserRepository userRepository;

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}
