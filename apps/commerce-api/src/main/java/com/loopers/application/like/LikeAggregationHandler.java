package com.loopers.application.like;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.infrastructure.cache.event.ProductEvent;
import com.loopers.infrastructure.cache.strategy.UpdateType;
import com.loopers.infrastructure.event.Envelope;
import com.loopers.infrastructure.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeAggregationHandler {

    private final ProductLikeService productLikeService;
    private final ApplicationEventPublisher publisher;

    @Async("likeAggregationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLiked(Envelope<ProductLikedEvent> envelope) {
        if (!EventType.PRODUCT_LIKED.getValue().equals(envelope.type())) return;

        ProductLikedEvent event = envelope.payload();
        log.info("Processing like aggregation - messageId: {}, eventId: {}, productId: {}, correlationId: {}",
            envelope.messageId(), event.eventId(), event.productId(), envelope.correlationId());

        productLikeService.incrementLike(event.productId());
        publisher.publishEvent(new ProductEvent(event.productId(), UpdateType.LIKE_CHANGED));
    }

    @Async("likeAggregationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnliked(Envelope<ProductUnlikedEvent> envelope) {
        if (!EventType.PRODUCT_UNLIKED.getValue().equals(envelope.type())) return;

        ProductUnlikedEvent event = envelope.payload();
        log.info("Processing unlike aggregation - messageId: {}, eventId: {}, productId: {}, correlationId: {}",
            envelope.messageId(), event.eventId(), event.productId(), envelope.correlationId());

        productLikeService.decrementLike(event.productId());
        publisher.publishEvent(new ProductEvent(event.productId(), UpdateType.LIKE_CHANGED));
    }
}
