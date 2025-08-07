package com.loopers.domain.point;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.loopers.application.point.AddPointCommand;
import com.loopers.application.point.PointServiceImpl;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.point.repository.PointRepository;
import com.loopers.domain.user.service.UserService;
import com.loopers.interfaces.api.point.PointResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private PointRepository pointRepository;


    @Test
    @DisplayName("포인트 충전을 하면 사용자의 포인트가 변경된다.")
    void charge_point_success() {
        // given
        String userId = "kth4909";
        BigDecimal chargeAmount = BigDecimal.valueOf(50000);

        AddPointCommand command = new AddPointCommand(userId, chargeAmount);
        given(userService.existsByUserId(userId)).willReturn(true);

        Point point = Point.of(userId, BigDecimal.ZERO);
        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(point));
        given(pointRepository.save(any(Point.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointResponse response = pointService.charge(command);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualByComparingTo(chargeAmount);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 충전 시 예외가 발생한다.")
    void charge_whenUserNotFound_shouldThrowException() {
        // given
        String userId = "kth490925424";
        BigDecimal chargeAmount = BigDecimal.valueOf(5000);
        given(userService.existsByUserId(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> pointService.charge(new AddPointCommand(userId, chargeAmount)))
            .isInstanceOf(CoreException.class)
            .extracting("errorType")
            .isEqualTo(ErrorType.NOT_FOUND);
    }
}
