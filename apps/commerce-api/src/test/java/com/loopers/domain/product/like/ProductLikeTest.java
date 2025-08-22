package com.loopers.domain.product.like;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductLike 도메인 단위 테스트")
class ProductLikeTest {

    @Test
    @DisplayName("정상 생성")
    void create_success() {
        // given
        Long productId = 100L;
        Integer likeCount = 5;

        // when
        ProductLike productLike = new ProductLike(productId, likeCount);

        // then
        assertThat(productLike).isNotNull();
        assertThat(productLike.getProductId()).isEqualTo(productId);
        assertThat(productLike.getLikeCount()).isEqualTo(likeCount);
    }

    @Test
    @DisplayName("초기 생성 시 좋아요 수는 0")
    void create_initial_like_count_is_zero() {
        // given
        Long productId = 100L;

        // when
        ProductLike productLike = ProductLike.create(productId);

        // then
        assertThat(productLike.getProductId()).isEqualTo(productId);
        assertThat(productLike.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 증가")
    void increment_like_success() {
        // given
        Long productId = 100L;
        ProductLike productLike = new ProductLike(productId, 5);

        // when
        ProductLike incrementedLike = productLike.incrementLike();

        // then
        assertThat(incrementedLike.getProductId()).isEqualTo(productId);
        assertThat(incrementedLike.getLikeCount()).isEqualTo(6);
        // 원본은 변경되지 않음 (불변객체)
        assertThat(productLike.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("좋아요 감소")
    void decrement_like_success() {
        // given
        Long productId = 100L;
        ProductLike productLike = new ProductLike(productId, 5);

        // when
        ProductLike decrementedLike = productLike.decrementLike();

        // then
        assertThat(decrementedLike.getProductId()).isEqualTo(productId);
        assertThat(decrementedLike.getLikeCount()).isEqualTo(4);
        // 원본은 변경되지 않음 (불변객체)
        assertThat(productLike.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("좋아요 감소 시 0보다 작아지지 않음")
    void decrement_like_minimum_zero() {
        // given
        Long productId = 100L;
        ProductLike productLike = new ProductLike(productId, 0);

        // when
        ProductLike decrementedLike = productLike.decrementLike();

        // then
        assertThat(decrementedLike.getProductId()).isEqualTo(productId);
        assertThat(decrementedLike.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 1에서 감소하면 0이 됨")
    void decrement_like_from_one_to_zero() {
        // given
        Long productId = 100L;
        ProductLike productLike = new ProductLike(productId, 1);

        // when
        ProductLike decrementedLike = productLike.decrementLike();

        // then
        assertThat(decrementedLike.getProductId()).isEqualTo(productId);
        assertThat(decrementedLike.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("연속 증가")
    void multiple_increment_like() {
        // given
        Long productId = 100L;
        ProductLike productLike = ProductLike.create(productId);

        // when
        ProductLike result = productLike
            .incrementLike()
            .incrementLike()
            .incrementLike();

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getLikeCount()).isEqualTo(3);
        // 원본은 여전히 0
        assertThat(productLike.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("증가 후 감소")
    void increment_then_decrement() {
        // given
        Long productId = 100L;
        ProductLike productLike = ProductLike.create(productId);

        // when
        ProductLike result = productLike
            .incrementLike()
            .incrementLike()
            .decrementLike();

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getLikeCount()).isEqualTo(1);
    }
}
