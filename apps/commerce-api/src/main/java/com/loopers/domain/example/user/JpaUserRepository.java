package com.loopers.domain.example.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  JpaUserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByUserId(String userId);

    boolean existsByUserId(String userId);
}
