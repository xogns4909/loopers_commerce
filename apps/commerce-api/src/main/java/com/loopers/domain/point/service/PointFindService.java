package com.loopers.domain.point.service;

import com.loopers.domain.point.model.Point;
import org.springframework.stereotype.Component;

@Component
public interface PointFindService {

    public Point findByUserId(String userId);

}
