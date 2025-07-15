package com.loopers.domain.example.user;

import org.springframework.data.jpa.repository.JpaRepository;


public interface  JpaUserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByUserId(UserId userId);
}
