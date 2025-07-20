package com.loopers.domain.user.service;

import com.loopers.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public interface UserFindService {

    public User findByUserId(String userId);

    public boolean existsByUserId(String userId);

}
