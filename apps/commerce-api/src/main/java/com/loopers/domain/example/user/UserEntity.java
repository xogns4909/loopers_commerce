package com.loopers.domain.example.user;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class UserEntity {

    private final UserId userId;
    private final Email email;
    private final Gender gender;
    private final BirthDay birthDay;

    @Builder
    public UserEntity(UserId userId, Email email, Gender gender, BirthDay birthDay) {
        this.userId = userId;
        this.email = email;
        this.gender = gender;
        this.birthDay = birthDay;
    }

    public static UserEntity of(String id, String email, String gender, String birthDay) {
        return new UserEntity(
            UserId.of(id),
            Email.of(email),
            Gender.of(gender),
            BirthDay.of(birthDay)
        );
    }
}
