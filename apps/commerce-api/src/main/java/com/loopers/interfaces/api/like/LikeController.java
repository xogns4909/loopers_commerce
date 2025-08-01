package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.UserCertifyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/like/products")
@RequiredArgsConstructor
public class LikeController {

    private final LikeFacade likeFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> likeProduct(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody LikeRequest request
    ) {
        UserCertifyUtil.extractUserId(userId);
        LikeResponse response = likeFacade.like(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<LikeResponse>> unlikeProduct(
        @RequestHeader("X-USER-ID") String userId,
        @RequestBody LikeRequest request
    ) {
        UserCertifyUtil.extractUserId(userId);
        LikeResponse response = likeFacade.unlike(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LikedProductResponse>>> getLikedProducts(
        @RequestHeader("X-USER-ID") String userId
    ) {
        UserCertifyUtil.extractUserId(userId);
        List<LikedProductResponse> likedProducts = likeFacade.getLikedProducts(userId);
        return ResponseEntity.ok(ApiResponse.success(likedProducts));
    }
}
