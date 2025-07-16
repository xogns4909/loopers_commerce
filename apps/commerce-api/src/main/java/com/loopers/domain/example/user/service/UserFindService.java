package com.loopers.domain.example.user.service;

import com.loopers.domain.example.user.model.User;
import org.springframework.stereotype.Component;

@Component
public interface UserFindService {

    public User findByUserId(String userId);

}
