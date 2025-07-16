package com.loopers.domain.example.point.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;


public record Balance(BigDecimal balance) {



    public Balance add(Balance amount) {
        validationAddBalance(amount);
        return new Balance(this.balance.add(amount.balance));
    }

    private static void validationAddBalance(Balance amount) {
        if (amount == null || amount.balance == null) {
            throw new CoreException(ErrorType.BAD_REQUEST,"충전 금액은 null일 수 없습니다.");
        }

        if (amount.isNegative()) {
            throw new CoreException(ErrorType.BAD_REQUEST,"충전 금액은 음수일 수 없습니다.");
        }
    }

    public boolean isNegative() {
        return balance.signum() < 0;
    }

}
