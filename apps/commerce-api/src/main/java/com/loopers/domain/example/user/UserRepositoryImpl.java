package com.loopers.domain.example.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository  {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public boolean findByUserId(String userId) {
        return true;
    }
}
