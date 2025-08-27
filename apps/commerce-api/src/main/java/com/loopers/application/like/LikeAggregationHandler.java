package com.loopers.application.like;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.infrastructure.cache.event.ProductEvent;
import com.loopers.infrastructure.cache.strategy.UpdateType;
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
    public void handleProductLiked(ProductLikedEvent event) {
        log.info("Processing like aggregation - eventId: {}, productId: {}", 
            event.eventId(), event.productId());
        

        productLikeService.incrementLike(event.productId());

        publisher.publishEvent(new ProductEvent(event.productId(), UpdateType.LIKE_CHANGED));
        

    }

    @Async("likeAggregationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        log.info("Processing unlike aggregation - eventId: {}, productId: {}", 
            event.eventId(), event.productId());

        productLikeService.decrementLike(event.productId());
        

        publisher.publishEvent(new ProductEvent(event.productId(), UpdateType.LIKE_CHANGED));

    }
}
