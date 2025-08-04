package com.loopers.infrastructure.like.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.like.model.Like;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@NoArgsConstructor
@AllArgsConstructor
public class LikeEntity extends BaseEntity {

    private String userId;

    private Long productId;

    private LocalDateTime likeAt;

    public static LikeEntity from(Like like) {
        return  new LikeEntity(like.userId().value(),like.productId(),LocalDateTime.now());
    }
}
