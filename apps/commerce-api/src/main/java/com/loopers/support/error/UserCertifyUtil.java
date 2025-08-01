package com.loopers.support.error;


import jakarta.servlet.http.HttpServletRequest;

public class UserCertifyUtil {


    public static String extractUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
        return userId;
    }
}

