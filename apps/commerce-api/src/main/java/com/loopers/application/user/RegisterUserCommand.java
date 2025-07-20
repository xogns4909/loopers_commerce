package com.loopers.application.user;

import com.loopers.domain.user.model.User;
import lombok.Builder;


@Builder
public record RegisterUserCommand(String userId, String email, String gender, String birthday) {

    public User toUserEntity() {
        return User.of(userId, email, gender, birthday);
    }

}
