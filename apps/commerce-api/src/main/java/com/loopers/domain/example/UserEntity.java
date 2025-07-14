package com.loopers.domain.example;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Builder;
import lombok.Getter;


@Getter
public class UserEntity  {

    private final String userId;
    private final String email;
    private final String name;
    private final String birthDate;

    @Builder
    public UserEntity(String userId, String email, String name, String birthDate) {
        if (userId == null || !userId.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        if (email == null || !email.matches("^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        if (birthDate == null || !birthDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        this.userId = userId;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
    }
}
