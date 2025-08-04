package com.loopers.application.point;

import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.domain.point.service.PointFindService;
import com.loopers.domain.point.service.PointService;
import com.loopers.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointFindService pointFindService;

    private final PointRepository pointRepository;

    @Override
    public boolean hasEnough(UserId userId, PaymentAmount amount) {
        Point point = pointFindService.findByUserId(userId.value());
        return point.getBalance().isGreaterThanOrEqual(Balance.of(amount.value()));
    }

    @Override
    public void deduct(UserId userId, PaymentAmount amount) {
        Point point = pointFindService.findByUserId(userId.value());
        point.deduct(Balance.of(amount.getAmount()));
        pointRepository.save(point);
    }
}

