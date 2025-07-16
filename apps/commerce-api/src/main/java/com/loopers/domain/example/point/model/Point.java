package com.loopers.domain.example.point.model;

import com.loopers.domain.example.user.model.UserId;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Point {

    private final UserId userId;
    private Balance balance;

    public Point(UserId userId, Balance initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
    }

    public Point charge(Balance amount) {
        this.balance = this.balance.add(amount);
        return this;
    }

    public static Point of(String userId, BigDecimal amount) {
        return new Point(UserId.of(userId), Balance.of(amount));
    }


}
