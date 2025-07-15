package com.loopers.domain.example.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class User {

    private final UserId userId;
    private final Email email;
    private final Gender gender;
    private final BirthDay birthDay;

    @Builder
    public User(UserId userId, Email email, Gender gender, BirthDay birthDay) {
        this.userId = userId;
        this.email = email;
        this.gender = gender;
        this.birthDay = birthDay;
    }

    public static User of(String id, String email, String gender, String birthDay) {
        return new User(
            UserId.of(id),
            Email.of(email),
            Gender.of(gender),
            BirthDay.of(birthDay)
        );
    }
}
