package com.loopers.application.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeResult;
import com.loopers.domain.like.model.Like;
import com.loopers.domain.user.model.UserId;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
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


        publisher.publishEvent(ProductLikedEvent.of(command.productId(), command.userId().value(), "like-flow"));
        return LikeResult.LIKED;
    }

    @Override
    @Transactional
    public LikeResult unlike(LikeCommand command) {
        if (!likeRepository.exists(command.userId(), command.productId())) {
            return LikeResult.NOT_LIKED;
        }
        likeRepository.delete(command.userId(), command.productId());


        publisher.publishEvent(ProductUnlikedEvent.of(command.productId(), command.userId().value(), "unlike-flow"));
        return LikeResult.UNLIKED;
    }

    @Override
    public List<LikedProductResponse> getLikedProductInfos(UserId id) {
        return likeRepository.findLikedProductsWithInfo(id);
    }
}
