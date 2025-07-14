package com.loopers.domain.example;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;


@Getter
public class UserEntity  {

    private final String userId;
    private final String email;
    private final String name;
    private final LocalDate birthDate;

    @Builder
    public UserEntity(String userId, String email, String name, LocalDate birthDate) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
    }
}
