package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.loopers.application.like.LikeCommand;
import com.loopers.application.like.LikeServiceImpl;
import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    @DisplayName("좋아요가 처음이면 LIKED 응답을 반환한다.")
    void like_firstTime_returnsLiked() {
        // given
        UserId userId = UserId.of("user1");
        Long productId = 1L;
        LikeCommand command = new LikeCommand(userId, productId);

        given(likeRepository.exists(userId, productId)).willReturn(false);

        // when
        LikeResult result = likeService.like(command);

        // then
        assertThat(result).isEqualTo(LikeResult.LIKED);
        verify(likeRepository).save(Like.create(userId, productId));
    }

    @Test
    @DisplayName("이미 좋아요를 누른 경우 ALREADY_LIKED를 반환한다.")
    void like_alreadyLiked_returnsAlreadyLiked() {
        // given
        UserId userId = UserId.of("user1");
        Long productId = 1L;
        LikeCommand command = new LikeCommand(userId, productId);

        given(likeRepository.exists(userId, productId)).willReturn(true);

        // when
        LikeResult result = likeService.like(command);

        // then
        assertThat(result).isEqualTo(LikeResult.ALREADY_LIKED);
    }

    @Test
    @DisplayName("좋아요 취소 성공 시 UNLIKED를 반환한다.")
    void unlike_success_returnsUnliked() {
        // given
        UserId userId = UserId.of("user1");
        Long productId = 1L;
        LikeCommand command = new LikeCommand(userId, productId);

        given(likeRepository.exists(userId, productId)).willReturn(true);

        // when
        LikeResult result = likeService.unlike(command);

        // then
        assertThat(result).isEqualTo(LikeResult.UNLIKED);
        verify(likeRepository).delete(userId, productId);
    }

    @Test
    @DisplayName("좋아요 취소 대상이 없으면 NOT_LIKED 반환")
    void unlike_notExist_returnsNotLiked() {
        // given
        UserId userId = UserId.of("user1");
        Long productId = 1L;
        LikeCommand command = new LikeCommand(userId, productId);

        given(likeRepository.exists(userId, productId)).willReturn(false);

        // when
        LikeResult result = likeService.unlike(command);

        // then
        assertThat(result).isEqualTo(LikeResult.NOT_LIKED);
    }

}

