package com.loopers.domain.example.point.service;

import com.loopers.domain.example.point.model.Point;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public interface PointFindService {

    public Point findByUserId(String userId);

}
