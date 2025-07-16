package com.loopers.application.example.point;

import com.loopers.domain.example.point.model.Point;
import com.loopers.domain.example.point.repository.PointRepository;
import com.loopers.domain.example.point.service.PointFindService;
import java.util.Optional;
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
