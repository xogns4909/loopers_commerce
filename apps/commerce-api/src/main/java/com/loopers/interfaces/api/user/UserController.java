package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserResponse;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        UserResponse response = userFacade.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse loginUser = userFacade.login(loginRequest);

        return ResponseEntity.ok()
            .header("X-USER-ID", loginUser.userId())
            .body(ApiResponse.success(loginUser));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
        @RequestHeader("X-USER-ID") String userId
    ) {
        // request 객체로 wrapping
        UserInfoRequest request = UserInfoRequest.fromHeader(userId);
        UserResponse response = userFacade.getUserInfo(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
