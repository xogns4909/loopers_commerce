package com.loopers.interfaces.api.point;


import com.loopers.application.point.PointFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.UserCertifyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointFacade pointFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<PointResponse>> findPoint(HttpServletRequest request) {
        String userId = UserCertifyUtil.extractUserId(request);
        PointResponse response = pointFacade.findPointInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<PointResponse>> chargePoint(@RequestBody PointChargeRequest chargeRequest,
        HttpServletRequest servletRequest
    ) {
        UserCertifyUtil.extractUserId(servletRequest);
        PointResponse response = pointFacade.chargePoint(chargeRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
