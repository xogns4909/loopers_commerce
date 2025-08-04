package com.loopers.application.point;

import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.domain.point.service.PointService;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.user.service.UserService;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {



    private final PointRepository pointRepository;

    private final UserService userService;

    @Override
    public boolean hasEnough(UserId userId, PaymentAmount amount) {
        Point point = findByUserId(userId.value());
        return point.getBalance().isGreaterThanOrEqual(Balance.of(amount.value()));
    }

    @Override
    public void deduct(UserId userId, PaymentAmount amount) {
        Point point = findByUserId(userId.value());
        point.deduct(Balance.of(amount.getAmount()));
        pointRepository.save(point);
    }

    @Override
    public Point findByUserId(String userId) {
        return pointRepository.findByUserId(userId).orElse(null);
    }


    @Override
    public PointResponse charge(AddPointCommand addPointCommand) {

        if(!userService.existsByUserId(addPointCommand.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        Point point = Optional.ofNullable(findByUserId(addPointCommand.userId()))
            .orElse(Point.of(addPointCommand.userId(), BigDecimal.ZERO));

        Point savePoint = pointRepository.save(point.charge(Balance.of(addPointCommand.amount())));

        return PointResponse.from(savePoint);
    }
}

