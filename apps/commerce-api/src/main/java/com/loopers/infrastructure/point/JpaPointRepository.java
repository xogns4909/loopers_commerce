package com.loopers.infrastructure.point;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPointRepository extends JpaRepository<com.loopers.infrastructure.point.PointEntity,Long> {

    PointEntity findByUserId(String userId);
}
