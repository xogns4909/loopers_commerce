package com.loopers.infrastructure.example.point;

import com.loopers.domain.example.point.model.Point;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPointRepository extends JpaRepository<PointEntity,Long> {

    PointEntity findByUserId(String userId);
}
