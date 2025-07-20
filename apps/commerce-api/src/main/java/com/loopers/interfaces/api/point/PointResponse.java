package com.loopers.interfaces.api.point;


import com.loopers.domain.point.model.Point;
import java.math.BigDecimal;

public record PointResponse(String userId, BigDecimal balance) {


    public static PointResponse from(Point point){
        return new PointResponse(
            point.getUserId().value(),
            point.getBalance().value()
        );
    }

}
