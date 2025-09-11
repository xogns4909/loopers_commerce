package com.loopers.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("랭킹 이벤트 타입 테스트")
class RankingEventTypeTest {
    
    @Test
    @DisplayName("기본 가중치가 올바르게 설정되어야 한다")
    void shouldHaveCorrectDefaultWeights() {
        assertThat(RankingEventType.PRODUCT_VIEWED.getDefaultWeight()).isEqualTo(0.1);
        assertThat(RankingEventType.PRODUCT_LIKED.getDefaultWeight()).isEqualTo(0.2);
        assertThat(RankingEventType.PRODUCT_UNLIKED.getDefaultWeight()).isEqualTo(-0.2);
        assertThat(RankingEventType.ORDER_CREATED.getDefaultWeight()).isEqualTo(0.7);
    }
    
    @Test
    @DisplayName("기본 가중치로 점