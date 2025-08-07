package com.loopers.domain.point.model;

import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Point {

    private Long id;
    private final UserId userId;
    private Balance balance;
    private Long version;

    public Point(UserId userId, Balance initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
    }

    public Point charge(Balance amount) {
        this.balance = this.balance.add(amount);
        return this;
    }

    public static Point reconstruct(Long id, String userId, BigDecimal balance, Long version) {
        Point p = new Point(UserId.of(userId), Balance.of(balance));
        p.id = id;
        p.version = version;
        return p;
    }

    public static Point of(String userId, BigDecimal balance) {
        return new Point( UserId.of(userId), Balance.of(balance));
    }

    public void deduct(Balance amount) {
        if (!this.balance.isGreaterThanOrEqual(amount)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }
}
