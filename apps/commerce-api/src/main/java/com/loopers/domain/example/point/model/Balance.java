package com.loopers.domain.example.point.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.Getter;


@Getter
public final class Balance {
    private final BigDecimal balance;

    private Balance(BigDecimal balance) {
        this.balance = balance;
    }

    public static Balance of(BigDecimal amount) {
        if (amount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 null일 수 없습니다.");
        }
        if (amount.signum() < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 음수일 수 없습니다.");
        }
        return new Balance(amount);
    }

    public Balance add(Balance amount) {
        return new Balance(this.balance.add(amount.balance));
    }

    public BigDecimal value() {
        return this.balance;
    }
}

