package com.loopers.domain.example.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserId {

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9]{1,10}$");
    private final String id;

    private UserId(String id) {
        this.id = id;
    }

    public static UserId of(String id) {
        validate(id);
        return new UserId(id);
    }

    private static void validate(String id) {
        if (id == null || !PATTERN.matcher(id).matches()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 아이디입니다.");
        }
    }
}
