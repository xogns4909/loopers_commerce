package com.loopers.application.example.point;

import com.loopers.domain.example.point.model.service.PointAddService;
import com.loopers.domain.example.point.service.PointFindService;
import com.loopers.interfaces.api.point.PointChargeRequest;
import com.loopers.interfaces.api.point.PointResponse;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointFacade {

    private final PointFindService pointFindService;

    private final PointAddService pointAddService;

    public PointResponse findPointInfo(String request) {
        return Optional.ofNullable(pointFindService.findByUserId(request))
            .map(PointResponse::from)
            .orElseGet(() -> new PointResponse(request, BigDecimal.ZERO));
    }

    public PointResponse chargePoint(PointChargeRequest pointChargeRequest){
        return pointAddService.charge(pointChargeRequest.toCommand(pointChargeRequest.userId(),pointChargeRequest.balance()));
    }


}
