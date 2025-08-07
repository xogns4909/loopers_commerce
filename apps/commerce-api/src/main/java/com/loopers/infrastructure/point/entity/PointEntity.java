package com.loopers.infrastructure.point.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.model.Balance;
import com.loopers.domain.point.model.Point;
import com.loopers.domain.user.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

    @Version
    private Long version;

    private PointEntity(String userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }



    public static PointEntity from(Point point) {
        PointEntity entity = new PointEntity(
            point.getUserId().value(),
            point.getBalance().value()
        );
        if (point.getId() != null) {
            entity.setId(point.getId());
        }
        entity.version = point.getVersion();
        return entity;
    }


    public Point toModel() {
        return Point.reconstruct(getId(), userId, balance,getVersion());
    }
}

