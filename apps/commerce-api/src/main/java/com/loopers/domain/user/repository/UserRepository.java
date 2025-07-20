package com.loopers.domain.user.repository;

import com.loopers.domain.user.model.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository {

    public Optional<User> findByUserId(String userId);

    public User save(User user);

    public boolean existsByUserId(String userId);
}
