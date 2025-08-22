package com.loopers.application.point;

import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.domain.point.service.PointService;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.user.service.UserService;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
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
    @HandleConcurrency(message = "포인트 사용에 실패했습니다. 다시 시도해주세요")
    public void deduct(UserId userId, PaymentAmount amount) {
        Point point = findByUserId(userId.value());
        point.deduct(Balance.of(amount.getAmount()));
        pointRepository.save(point);
    }

    @Override
    @HandleConcurrency(message = "포인트 복구에 실패했습니다. 다시 시도해주세요")
    public void restore(UserId userId, PaymentAmount amount) {

        Point point = findByUserId(userId.value());
        if (point != null) {
            Point restored = point.charge(Balance.of(amount.getAmount()));
            pointRepository.save(restored);
        }
    }

    @Override
    public Point findByUserId(String userId) {
        return pointRepository.findByUserId(userId).orElse(null);
    }


    @Override
    @HandleConcurrency(message = "포인트 충전에 실패했습니다. 다시 시도해주세요")
    public PointResponse charge(AddPointCommand cmd) {
        if (!userService.existsByUserId(cmd.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        Point point = findByUserId(cmd.userId());
        if (point == null) {
            point = Point.reconstruct(null, cmd.userId(), BigDecimal.ZERO, null);
        }

        Point updated = point.charge(Balance.of(cmd.amount()));
        Point saved = pointRepository.save(updated);

        return PointResponse.from(saved);
    }
}

