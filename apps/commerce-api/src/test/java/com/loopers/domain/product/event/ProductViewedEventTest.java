package com.loopers.domain.product.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.user.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductViewedEventTest {

    @Test
    @DisplayName("개별 상품 조회 이벤트 생성")
    void ofSingleView_creates_single_view_event() {
        // given
        Long productId = 1L;
        UserId userId = UserId.of("testUser");

        // when
        ProductViewedEvent event = ProductViewedEvent.ofSingleView(productId, userId);

        // then
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.userId()).isEqualTo(userId.value());
        assertThat(event.viewType()).isEqualTo("SINGLE");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.viewedAt()).isNotNull();
    }

    @Test
    @DisplayName("목록 조회 이벤트 생성")
    void ofListView_creates_list_view_event() {
        // given
        Long productId = 2L;
        UserId userId = UserId.of("listUser");

        // when
        ProductViewedEvent event = ProductViewedEvent.ofListView(productId, userId);

        // then
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.userId()).isEqualTo(userId.value());
        assertThat(event.viewType()).isEqualTo("LIST");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.viewedAt()).isNotNull();
    }

    @Test
    @DisplayName("각 이벤트는 고유한 eventId를 가진다")
    void each_event_has_unique_eventId() {
        // given
        Long productId = 1L;
        UserId userId = UserId.of("testUser");

        // when
        ProductViewedEvent event1 = ProductViewedEvent.ofSingleView(productId, userId);
        ProductViewedEvent event2 = ProductViewedEvent.ofSingleView(productId, userId);

        // then
        assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
    }
}
