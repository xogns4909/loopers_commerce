package com.loopers.infrastructure.point;

import com.loopers.infrastructure.point.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPointRepository extends JpaRepository<PointEntity,Long> {

    PointEntity findByUserId(String userId);
}
