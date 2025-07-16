package com.loopers.infrastructure.user.entity;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    private String userId;
    private String email;
    private String gender;
    private String birthday;

    public UserEntity(String userId, String email, String gender, String birthday) {
        this.userId = userId;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
    }


    public User toDomain() {
        return User.of(userId, email, gender, birthday);
    }

    public static UserEntity fromDomain(User user) {
        return new UserEntity(
            user.getUserId().value(),
            user.getEmail().value(),
            user.getGender().name(),
            user.getBirthDay().value().toString()
        );
    }
}
