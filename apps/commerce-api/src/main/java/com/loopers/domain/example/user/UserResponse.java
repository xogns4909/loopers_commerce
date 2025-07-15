package com.loopers.domain.example.user;

import java.time.LocalDate;


public record UserResponse(String userId, String email, String gender, LocalDate birthday) {

    public static UserResponse from(User entity) {
        return new UserResponse(
            entity.getUserId().value(),
            entity.getEmail().value(),
            entity.getGender().name(),
            entity.getBirthDay().value()
        );
    }
}
