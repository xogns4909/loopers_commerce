package com.loopers.application.example.point;

import com.loopers.application.example.user.UserResponse;
import com.loopers.domain.example.point.model.service.PointAddService;
import com.loopers.domain.example.point.service.PointFindService;
import com.loopers.interfaces.api.point.PointInfoRequest.PointInfoRequest;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.interfaces.api.user.UserInfoRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointFacade {

    private final PointFindService pointFindService;

    public PointResponse findPointInfo(PointInfoRequest request) {
        return Optional.ofNullable(pointFindService.findByUserId(request.userId()))
            .map(PointResponse::from)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저입니다."));
    }
}
