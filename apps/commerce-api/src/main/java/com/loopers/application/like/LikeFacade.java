package com.loopers.application.like;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeResult;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.like.LikeRequest;
import com.loopers.interfaces.api.like.LikeResponse;
import com.loopers.interfaces.api.like.LikedProductResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LikeResponse like(LikeRequest request) {
        
        if(!productService.existsProduct(request.productId())){
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        LikeCommand command = toCommand(request);
        LikeResult result = likeService.like(command);

        if (result == LikeResult.LIKED) {
            eventPublisher.publishEvent(
                ProductLikedEvent.of(request.productId(), request.userId(), "temp-correlation-id")
            );
        }

        return switch (result) {
            case LIKED -> LikeResponse.liked(request.productId());
            case ALREADY_LIKED -> LikeResponse.alreadyLiked(request.productId());
            default -> throw new CoreException(ErrorType.INTERNAL_ERROR);
        };
    }

    @Transactional
    public LikeResponse unlike(LikeRequest request) {
        
        LikeCommand command = toCommand(request);
        LikeResult result = likeService.unlike(command);

        if (result == LikeResult.UNLIKED) {
            eventPublisher.publishEvent(
                ProductUnlikedEvent.of(request.productId(), request.userId(), "temp-correlation-id")
            );
        }

        return switch (result) {
            case UNLIKED -> LikeResponse.unliked(request.productId());
            case NOT_LIKED -> LikeResponse.notLiked(request.productId());
            default -> throw new CoreException(ErrorType.INTERNAL_ERROR);
        };
    }

    public List<LikedProductResponse> getLikedProducts(String userId) {
        return likeService.getLikedProductInfos(UserId.of(userId));
    }

    private LikeCommand toCommand(LikeRequest request) {
        return new LikeCommand(UserId.of(request.userId()), request.productId());
    }
}
