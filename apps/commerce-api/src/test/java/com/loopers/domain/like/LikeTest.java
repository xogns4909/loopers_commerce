package com.loopers.domain.like;

import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LikeTest {

    @Test
    @DisplayName("정상 생성")
    void create_success() {
        // given
        String userId = "kth4909";
        Long productId = 100L;

        // when
        Like like = Like.reconstruct(1L , UserId.of(userId), productId);

        // then
        assertThat(like).isNotNull();
        assertThat(like.userId().value()).isEqualTo(userId);
        assertThat(like.productId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("userId가 CoreException 404 예외 발생")
    void invalid_null_userId() {
        // given
        Long productId = 100L;

        // when & then
        assertThatThrownBy(() -> Like.reconstruct(1L, null, productId))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException ex = (CoreException) e;
                assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            });
    }

    @Test
    @DisplayName("productId가 null이면 CoreException 404 예외 발생")
    void invalid_null_productId() {
        // given
        String userId = "userId";

        // when & then
        assertThatThrownBy(() -> Like.reconstruct(1L, UserId.of(userId), null))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> {
                CoreException ex = (CoreException) e;
                assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            });
    }
}
