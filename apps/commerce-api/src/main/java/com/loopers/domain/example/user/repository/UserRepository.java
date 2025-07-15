package com.loopers.domain.example.user.repository;

import com.loopers.domain.example.user.model.User;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository {

    public User findByUserId(String userId);

    public User save(User user);

    public boolean existsByUserId(String userId);
}
