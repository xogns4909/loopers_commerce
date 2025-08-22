package com.loopers.domain.product.like;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.loopers.application.product.like.ProductLikeServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductLikeService 단위 테스트 ")
class ProductLikeServiceTest {

    @Mock
    private ProductLikeRepository productLikeRepository;

    @InjectMocks
    private ProductLikeServiceImpl productLikeService;

    private final Long productId = 100L;

    @Test
    @DisplayName("좋아요 증가 - 기존 레코드 없음이면 0→1로 저장")
    void increment_creates_when_absent() {
        // given
        given(productLikeRepository.findByProductIdWithLock(productId)).willReturn(Optional.empty());
        ArgumentCaptor<ProductLike> captor = ArgumentCaptor.forClass(ProductLike.class);
        given(productLikeRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        productLikeService.incrementLike(productId);

        // then
        ProductLike saved = captor.getValue();
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getLikeCount()).isEqualTo(1);
        then(productLikeRepository).should().findByProductIdWithLock(productId);
        then(productLikeRepository).should().save(any(ProductLike.class));
    }

    @Test
    @DisplayName("좋아요 증가 - 기존 10이면 11로 저장")
    void increment_updates_when_exists() {
        // given
        ProductLike existing = ProductLike.of(productId, 10);
        given(productLikeRepository.findByProductIdWithLock(productId)).willReturn(Optional.of(existing));
        ArgumentCaptor<ProductLike> captor = ArgumentCaptor.forClass(ProductLike.class);
        given(productLikeRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        productLikeService.incrementLike(productId);

        // then
        assertThat(captor.getValue().getLikeCount()).isEqualTo(11);
        then(productLikeRepository).should().findByProductIdWithLock(productId);
        then(productLikeRepository).should().save(any(ProductLike.class));
    }

    @Test
    @DisplayName("좋아요 감소 - 0 이하로 내려가지 않음")
    void decrement_never_below_zero() {
        // given
        ProductLike existing = ProductLike.of(productId, 0);
        given(productLikeRepository.findByProductIdWithLock(productId)).willReturn(Optional.of(existing));
        ArgumentCaptor<ProductLike> captor = ArgumentCaptor.forClass(ProductLike.class);
        given(productLikeRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        productLikeService.decrementLike(productId);

        // then
        assertThat(captor.getValue().getLikeCount()).isEqualTo(0);
        then(productLikeRepository).should().findByProductIdWithLock(productId);
        then(productLikeRepository).should().save(any(ProductLike.class));
    }

    @Test
    @DisplayName("좋아요 수 조회 - 존재하면 값 반환")
    void get_count_when_exists() {
        // given
        given(productLikeRepository.findByProductId(productId))
            .willReturn(Optional.of(ProductLike.of(productId, 7)));

        // when
        int count = productLikeService.getLikeCount(productId);

        // then
        assertThat(count).isEqualTo(7);
        then(productLikeRepository).should().findByProductId(productId);
    }

    @Test
    @DisplayName("좋아요 수 조회 - 없으면 0")
    void get_count_when_absent() {
        // given
        given(productLikeRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when
        int count = productLikeService.getLikeCount(productId);

        // then
        assertThat(count).isEqualTo(0);
        then(productLikeRepository).should().findByProductId(productId);
    }
}
