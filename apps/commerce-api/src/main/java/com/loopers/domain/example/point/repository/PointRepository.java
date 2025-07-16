package com.loopers.domain.example.point.repository;

import com.loopers.domain.example.point.model.Point;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface PointRepository {

    public Optional<Point> findByUserId(String UserId);

    public Point save(Point point);
}
