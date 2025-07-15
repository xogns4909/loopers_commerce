package com.loopers.domain.example.user;

public interface UserRepository {

    public User findByUserId(String userId);

    public User save(User user);

    public boolean existsByUserId(String userId);
}
