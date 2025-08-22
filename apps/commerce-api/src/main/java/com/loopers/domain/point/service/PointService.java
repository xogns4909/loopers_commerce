package com.loopers.domain.point.service;

import com.loopers.application.point.AddPointCommand;
import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.point.PointResponse;

public interface PointService {
    boolean hasEnough(UserId userId, PaymentAmount amount);
    void deduct(UserId userId, PaymentAmount amount);
    void restore(UserId userId, PaymentAmount amount);
    Point findByUserId(String userId);
    PointResponse charge(AddPointCommand addPointCommand);
}
