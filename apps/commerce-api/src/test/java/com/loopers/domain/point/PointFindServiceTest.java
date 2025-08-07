package com.loopers.domain.point;


import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

import com.loopers.application.point.PointServiceImpl;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointFindServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    @DisplayName("해당 ID 의 포인트 정보가 존재할 경우, 포인트 객체가 반환된다.")
    void find_pointInfo_success() {
        // given
        String userId = "kth4909";
        Point point = Point.of(userId, (BigDecimal.valueOf(1000)));

        given(pointRepository.findByUserId(userId))
            .willReturn(Optional.of(point));

        // when
        Point foundPoint = pointService.findByUserId(userId);

        // then
        then(foundPoint).isNotNull();
        then(foundPoint.getUserId().value()).isEqualTo(userId);
        then(foundPoint.getBalance().getBalance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("해당 ID 의 포인트 정보가 존재하지 않을 경우, null 이 반환된다.")
    void find_pointInfo_fail() {
        // given
        String userId = "nonExistentUser";
        given(pointRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when
        Point result = pointService.findByUserId(userId);

        // then
        Assertions.assertThat(result).isNull();
    }
}
