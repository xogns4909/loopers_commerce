package com.loopers.domain.point.model.service;

import com.loopers.application.point.AddPointCommand;
import com.loopers.interfaces.api.point.PointResponse;
import org.springframework.stereotype.Component;

@Component
public interface PointAddService {

    public PointResponse charge(AddPointCommand addPointCommand);

}
