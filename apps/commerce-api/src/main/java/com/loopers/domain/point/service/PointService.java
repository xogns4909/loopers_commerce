package com.loopers.domain.point.service;

import com.loopers.domain.payment.model.PaymentAmount;
import com.loopers.domain.user.model.UserId;

public interface PointService {
    boolean hasEnough(UserId userId, PaymentAmount amount);
    void deduct(UserId userId, PaymentAmount amount);
}
