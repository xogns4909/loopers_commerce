package com.loopers.domain.example.point;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.loopers.application.example.point.AddPointCommand;
import com.loopers.application.example.point.PointAddServiceImpl;
import com.loopers.application.example.point.PointFindServiceImpl;
import com.loopers.domain.example.point.model.Point;
import com.loopers.domain.example.point.repository.PointRepository;
import com.loopers.domain.example.user.service.UserFindService;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointAddServiceTest {

    @Mock
    private UserFindService userFindService;

    @Mock
    private PointFindServiceImpl pointFindService;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointAddServiceImpl pointAddService;

    @Test
    @DisplayName("포인트 충전을 하면 사용자의 포인트가 변경된다.")
    void charge_point_success() {
        // given
        String userId = "kth4909";
        BigDecimal chargeAmount = BigDecimal.valueOf(50000);

        AddPointCommand command = new AddPointCommand(userId, chargeAmount);
        given(userFindService.existsByUserId(userId)).willReturn(true);

        Point point = Point.of(userId, BigDecimal.ZERO);
        given(pointFindService.findByUserId(userId)).willReturn(point);
        given(pointRepository.save(any(Point.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointResponse response = pointAddService.charge(command);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualByComparingTo(chargeAmount);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 충전 시 예외가 발생한다.")
    void charge_whenUserNotFound_shouldThrowException() {
        // given
        String userId = "kth4909";
        BigDecimal chargeAmount = BigDecimal.valueOf(5000);
        given(userFindService.existsByUserId(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> pointAddService.charge(new AddPointCommand(userId, chargeAmount)))
            .isInstanceOf(CoreException.class)
            .extracting("errorType")
            .isEqualTo(ErrorType.NOT_FOUND);
    }
}
