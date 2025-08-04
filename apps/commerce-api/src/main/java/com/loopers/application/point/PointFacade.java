package com.loopers.application.point;

import com.loopers.domain.point.service.PointService;
import com.loopers.interfaces.api.point.PointChargeRequest;
import com.loopers.interfaces.api.point.PointResponse;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointFacade {

    private final PointService pointService;

    public PointResponse findPointInfo(String request) {
        return Optional.ofNullable(pointService.findByUserId(request))
            .map(PointResponse::from)
            .orElseGet(() -> new PointResponse(request, BigDecimal.ZERO));
    }

    public PointResponse chargePoint(PointChargeRequest pointChargeRequest){
        return pointService.charge(pointChargeRequest.toCommand(pointChargeRequest.userId(),pointChargeRequest.balance()));
    }


}
