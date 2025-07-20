package com.loopers.domain.point.repository;

import com.loopers.domain.point.model.Point;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface PointRepository {

    public Optional<Point> findByUserId(String UserId);

    public Point save(Point point);
}
