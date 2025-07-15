package com.loopers.domain.example.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


//ToDo 유효성 검증 로직 추가
@Getter
@AllArgsConstructor
@Builder
public class RegisterUserCommand {

    private String userId;

    private String email;

    private String gender;

    private String birthday;

    public UserEntity toUserEntity() {
        return UserEntity.of(userId,email,gender,birthday);
    }

}
