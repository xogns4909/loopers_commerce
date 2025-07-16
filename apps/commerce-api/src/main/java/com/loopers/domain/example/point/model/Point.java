package com.loopers.domain.example.point.model;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Point {

    private final String userId;
    private Balance balance;

    public Point(String userId, Balance initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
    }

    public void charge(Balance amount) {
        this.balance = this.balance.add(amount);
    }


    public BigDecimal currentBalance() {
        return balance.balance();
    }
}
