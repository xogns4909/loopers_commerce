package com.loopers.infrastructure.like.entity;

import com.loopers.domain.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;


@Entity
@Table(name = "likes")
public class LikeEntity extends BaseEntity {

    private LocalDateTime likeAt;

    private Long productId;

    private Long userId;
}
