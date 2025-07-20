package com.loopers.support.error;


import jakarta.servlet.http.HttpServletRequest;

public class UserCertifyUtil {

    private static final String USER_ID_HEADER = "X-USER-ID";

    public static String extractUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
        return userId;
    }
}

