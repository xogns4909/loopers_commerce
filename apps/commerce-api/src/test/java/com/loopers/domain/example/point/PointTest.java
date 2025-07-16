package com.loopers.domain.example.point;

import com.loopers.domain.example.point.model.Balance;
import com.loopers.domain.example.point.model.Point;
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

        assertThat(point.getUserId()).isEqualTo("kth4909");
        assertThat(point.getBalance().balance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("포인트를 정상적으로 충전할 수 있다.")
    void charge_success() {
        Point point = Point.of("kth4909",BigDecimal.ZERO);

        point.charge(new Balance(BigDecimal.valueOf(1000)));

        assertThat(point.getBalance().balance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("0 이하 금액으로 충전 시 예외가 발생한다.")
    void charge_fail_invalid_amount() {
        Point point = Point.of("kth4909",BigDecimal.ZERO);

        assertThatThrownBy(() -> point.charge(new Balance(BigDecimal.valueOf(-1))))
            .isInstanceOf(CoreException.class)
            .hasMessage("충전 금액은 음수일 수 없습니다.");

    }

    @Test
    @DisplayName("음수 잔액으로 Point 생성 시 예외 발생")
    void create_point_with_negative_balance() {
        assertThatThrownBy(() -> Point.of("kth4909",BigDecimal.valueOf(-10000)))
            .isInstanceOf(CoreException.class)
            .hasMessage("잔액은 0원 이상이어야 합니다.");
    }
}
