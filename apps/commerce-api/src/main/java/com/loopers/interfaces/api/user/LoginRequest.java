package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.NotBlank;


public record LoginRequest(
    @NotBlank(message = "아이디는 필수입니다.") String userId) {

}
