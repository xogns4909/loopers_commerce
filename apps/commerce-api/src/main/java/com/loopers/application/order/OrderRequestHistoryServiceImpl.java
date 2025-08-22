package com.loopers.application.order;

import com.loopers.domain.order.OrderRequestHistoryRepository;
import com.loopers.domain.order.OrderRequestHistoryService;
import com.loopers.domain.order.model.OrderRequestHistory;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRequestHistoryServiceImpl implements OrderRequestHistoryService {

    private final OrderRequestHistoryRepository repository;

    @Override
    public void saveReceived(String idempotencyKey, String userId, Long orderId) {
        OrderRequestHistory history = OrderRequestHistory.of(idempotencyKey, userId, orderId);
        repository.save(history);
    }

    @Override
    public void markAccepted(String idempotencyKey) {
        OrderRequestHistory history = findOrThrow(idempotencyKey);
        repository.save(history.markSuccess());
    }

    @Override
    public void markFailure(String idempotencyKey) {
        OrderRequestHistory history = findOrThrow(idempotencyKey);
        repository.save(history.markFailure());
    }

    @Override
    public Optional<Long> findOrderIdByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
            .map(OrderRequestHistory::orderId);
    }

    private OrderRequestHistory findOrThrow(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문 요청 히스토리를 찾을 수 없습니다."));
    }
}
