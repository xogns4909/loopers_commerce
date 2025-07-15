package com.loopers.domain.example.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Email {

    private static final Pattern PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+\\.[A-Za-z]{2,}$");
    private final String email;

    private Email(String email) {
        this.email = email;
    }

    public static Email of(String email) {
        validate(email);
        return new Email(email);
    }

    private static void validate(String email) {
        if (email == null || !PATTERN.matcher(email).matches()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 이메일 형식입니다.");
        }
    }
}
