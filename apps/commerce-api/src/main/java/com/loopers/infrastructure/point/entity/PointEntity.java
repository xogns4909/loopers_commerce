package com.loopers.infrastructure.point.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.user.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "amount")
@Getter
@NoArgsConstructor
public class PointEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    private PointEntity(String userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }



    public static PointEntity from(Point point) {
        PointEntity entity = new PointEntity(point.getUserId().value(), point.getBalance().value());
        entity.setId(point.getId());
        return entity;
    }

    public Point toModel() {
        return Point.reconstruct(getId(), userId, balance);
    }
}
