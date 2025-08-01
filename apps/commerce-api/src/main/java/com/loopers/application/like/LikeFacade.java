package com.loopers.application.like;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.like.LikeRequest;
import com.loopers.interfaces.api.like.LikeResponse;
import com.loopers.interfaces.api.like.LikedProductResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;

    public LikeResponse like(LikeRequest request) {

        if(!productService.existsProduct(request.productId())){
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        LikeCommand command = toCommand(request);
        LikeResult result = likeService.like(command);

        return switch (result) {
            case LIKED -> LikeResponse.liked(request.productId());
            case ALREADY_LIKED -> LikeResponse.alreadyLiked(request.productId());
            default -> throw new CoreException(ErrorType.INTERNAL_ERROR);
        };
    }

    public LikeResponse unlike(LikeRequest request) {
        LikeCommand command = toCommand(request);
        LikeResult result = likeService.unlike(command);

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
