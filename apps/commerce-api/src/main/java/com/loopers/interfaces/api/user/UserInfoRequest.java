package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.NotBlank;

public record UserInfoRequest(
    @NotBlank(message = "인증되지 않은 회원입니다.") String userId
) {
    public static UserInfoRequest fromHeader(String userId) {
        return new UserInfoRequest(userId);
    }
}
