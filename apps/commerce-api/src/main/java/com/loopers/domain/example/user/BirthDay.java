package com.loopers.domain.example.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BirthDay {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LocalDate birthDay;

    private BirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public static BirthDay of(String text) {
        validate(text);
        LocalDate parsed = parse(text);
        return new BirthDay(parsed);
    }

    private static void validate(String text) {
        if (text == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 생년월일 형식입니다.");
        }
    }

    private static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 생년월일 형식입니다.");
        }
    }



}
