package com.loopers.domain.order;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.loopers.application.order.OrderRequestHistoryServiceImpl;
import com.loopers.domain.order.model.OrderRequestHistory;
import com.loopers.domain.order.model.OrderRequestStatus;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderRequestHistoryServiceTest {

    @Mock
    private OrderRequestHistoryRepository repository;

    @InjectMocks
    private OrderRequestHistoryServiceImpl service;

    @Test
    @DisplayName("savePending: 히스토리를 PENDING 상태로 저장한다")
    void savePending_success() {
        // given
        String key = "idem-001";
        String userId = "user1";
        Long orderId = 123L;

        // when
        service.saveReceived(key, userId, orderId);

        // then
        verify(repository).save(argThat(history ->
            history.idempotencyKey().equals(key) &&
                history.userId().equals(userId) &&
                history.orderId().equals(orderId) &&
                history.status() == OrderRequestStatus.PENDING
        ));
    }

    @Test
    @DisplayName("markSuccess: 기존 히스토리를 SUCCESS로 변경해 저장한다")
    void markSuccess_success() {
        // given
        String key = "idem-002";
        OrderRequestHistory existing = OrderRequestHistory.of(key, "user1", 456L);
        given(repository.findByIdempotencyKey(key)).willReturn(Optional.of(existing));

        // when
        service.markAccepted(key);

        // then
        verify(repository).save(argThat(h -> h.status() == OrderRequestStatus.SUCCESS));
    }

    @Test
    @DisplayName("markFailure: 기존 히스토리를 FAILURE로 변경해 저장한다")
    void markFailure_success() {
        // given
        String key = "idem-003";
        OrderRequestHistory existing = OrderRequestHistory.of(key, "user1", 789L);
        given(repository.findByIdempotencyKey(key)).willReturn(Optional.of(existing));

        // when
        service.markFailure(key);

        // then
        verify(repository).save(argThat(h -> h.status() == OrderRequestStatus.FAILURE));
    }

    @Test
    @DisplayName("markSuccess: 존재하지 않는 히스토리에 대해 예외를 발생시킨다")
    void markSuccess_notFound_throwsException() {
        // given
        String key = "not-found";
        given(repository.findByIdempotencyKey(key)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.markAccepted(key))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("주문 요청 히스토리를 찾을 수 없습니다.");
    }

}
