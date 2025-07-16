package com.loopers.application.point;

import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.domain.point.service.PointFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointFindServiceImpl implements PointFindService {

    private final PointRepository pointRepository;

    @Override
    public Point findByUserId(String userId) {
        return pointRepository.findByUserId(userId).orElse(null);
    }
}
