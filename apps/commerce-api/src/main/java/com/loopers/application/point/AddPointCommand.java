package com.loopers.application.point;

import com.loopers.domain.point.model.Point;
import java.math.BigDecimal;




public record AddPointCommand(String userId, BigDecimal amount) {

    public Point toPoint() {
        return Point.of(userId, amount);
    }

}
