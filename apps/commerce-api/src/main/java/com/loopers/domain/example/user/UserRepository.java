package com.loopers.domain.example.user;

import org.springframework.stereotype.Component;

@Component
public interface UserRepository {

    public User findByUserId(String userId);

    public User save(User user);

    public boolean existsByUserId(String userId);
}
