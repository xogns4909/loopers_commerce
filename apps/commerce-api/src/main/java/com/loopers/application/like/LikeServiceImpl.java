package com.loopers.application.like;

import com.loopers.infrastructure.cache.strategy.UpdateType;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeResult;
import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.cache.event.ProductEvent;
import com.loopers.interfaces.api.like.LikedProductResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    public LikeResult like(LikeCommand command) {
        if (likeRepository.exists(command.userId(), command.productId())) {
            return LikeResult.ALREADY_LIKED;
        }
        likeRepository.save(Like.create(command.userId(), command.productId()));

        publisher.publishEvent(new ProductEvent(command.productId(), UpdateType.LIKE_CHANGED));
        return LikeResult.LIKED;
    }

    @Override
    @Transactional
    public LikeResult unlike(LikeCommand command) {
        if (!likeRepository.exists(command.userId(), command.productId())) {
            return LikeResult.NOT_LIKED;
        }
        likeRepository.delete(command.userId(), command.productId());

        publisher.publishEvent(new ProductEvent(command.productId(), UpdateType.LIKE_CHANGED));
        return LikeResult.UNLIKED;
    }

    @Override
    public List<LikedProductResponse> getLikedProductInfos(UserId id) {
        return likeRepository.findLikedProductsWithInfo(id);
    }
}
