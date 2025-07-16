package com.loopers.infrastructure.example.user;

import com.loopers.domain.example.user.model.User;
import com.loopers.infrastructure.example.user.entity.UserEntity;
import com.loopers.domain.example.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByUserId(String userId) {
        return Optional.ofNullable(jpaUserRepository.findByUserId(userId))
            .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(UserEntity.fromDomain(user)).toDomain();
    }

    @Override
    public boolean existsByUserId(String userId) {
        return jpaUserRepository.existsByUserId(userId);
    }
}
