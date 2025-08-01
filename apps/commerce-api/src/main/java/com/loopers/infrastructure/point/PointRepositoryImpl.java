package com.loopers.infrastructure.point;

import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.infrastructure.point.entity.PointEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final JpaPointRepository jpaPointRepository;

    @Override
    public Optional<Point> findByUserId(String userId) {
        return Optional.ofNullable(jpaPointRepository.findByUserId(userId))
            .map(PointEntity::toModel);
    }

    @Override
    public Point save(Point point) {
        return jpaPointRepository.save(PointEntity.from(point)).toModel();
    }
}
