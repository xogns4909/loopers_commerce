package com.loopers.domain.common;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class Money {

    protected final BigDecimal amount;

    protected Money(BigDecimal amount) {
        validate(amount);
        this.amount = amount;
    }

    private void validate(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 0 이상이어야 합니다.");
        }
    }
}
