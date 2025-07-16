package com.loopers.domain.point;

import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class PointTest {

    @Test
    @DisplayName("Point 객체가 정상 생성된다.")
    void create_point_success() {
        Point point = Point.of("kth4909",BigDecimal.ZERO);

        assertThat(point.getUserId().value()).isEqualTo("kth4909");
        assertThat(point.getBalance().getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("포인트를 정상적으로 충전할 수 있다.")
    void charge_success() {
        Point point = Point.of("kth4909",BigDecimal.ZERO);

        point.charge(Balance.of(BigDecimal.valueOf(1000)));

        assertThat(point.getBalance().getBalance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("0 이하 금액으로 충전 시 예외가 발생한다.")
    void charge_fail_invalid_amount() {
        Point point = Point.of("kth4909",BigDecimal.ZERO);

        assertThatThrownBy(() -> point.charge(Balance.of(BigDecimal.valueOf(-1))))
            .isInstanceOf(CoreException.class)
            .hasMessage("금액은 음수일 수 없습니다.");

    }

}
