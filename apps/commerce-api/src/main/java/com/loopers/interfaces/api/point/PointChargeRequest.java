package com.loopers.interfaces.api.point;

import com.loopers.application.example.point.AddPointCommand;
import java.math.BigDecimal;

public record PointChargeRequest(String userId, BigDecimal balance) {

    public AddPointCommand toCommand(String userId,BigDecimal balance) {
        return new AddPointCommand(userId,balance);
    }
}
