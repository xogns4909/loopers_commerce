package com.loopers.domain.example.user.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;


@Getter
public enum Gender {

    M, F;

    public static Gender of(String value) {
        if (value == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 성별입니다.");
        }
        try {
            return Gender.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 성별입니다.");
        }
    }
}
