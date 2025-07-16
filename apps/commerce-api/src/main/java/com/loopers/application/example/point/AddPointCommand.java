package com.loopers.application.example.point;

import com.loopers.domain.example.point.model.Balance;
import com.loopers.domain.example.point.model.Point;
import java.math.BigDecimal;




public record AddPointCommand(String userId, BigDecimal amount) {

    public Point toPoint() {
        return Point.of(userId, amount);
    }

}
