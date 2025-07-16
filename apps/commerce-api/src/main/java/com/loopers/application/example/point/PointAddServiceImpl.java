package com.loopers.application.example.point;

import com.loopers.domain.example.point.model.Balance;
import com.loopers.domain.example.point.model.Point;
import com.loopers.domain.example.point.model.service.PointAddService;
import com.loopers.domain.example.point.repository.PointRepository;
import com.loopers.domain.example.point.service.PointFindService;
import com.loopers.domain.example.user.service.UserFindService;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointAddServiceImpl implements PointAddService {

    private final UserFindService userFindService;

    private final PointFindService pointFindService;

    private final PointRepository pointRepository;

    @Override
    public PointResponse charge(AddPointCommand addPointCommand) {
        if(!userFindService.existsByUserId(addPointCommand.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        Point point = Optional.ofNullable(pointFindService.findByUserId(addPointCommand.userId()))
            .orElse(Point.of(addPointCommand.userId(), BigDecimal.ZERO));

        Point savePoint = pointRepository.save(point.charge(Balance.of(addPointCommand.amount())));

        return PointResponse.from(savePoint);
    }

}
