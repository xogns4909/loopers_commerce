package com.loopers.infrastructure.user;

import com.loopers.infrastructure.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  JpaUserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByUserId(String userId);

    boolean existsByUserId(String userId);
}
