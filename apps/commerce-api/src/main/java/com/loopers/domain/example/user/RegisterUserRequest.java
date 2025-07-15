package com.loopers.domain.example.user;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
    @NotBlank(message = "아이디는 필수입니다.") String userId,
    @NotBlank(message = "이메일은 필수입니다.") String email,
    @NotBlank(message = "성별은 필수입니다.") String gender,
    @NotBlank(message = "생년월일은 필수입니다.") String birthday
) {

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(userId, email, gender, birthday);
    }
}
