package com.loopers.application.product;

import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.event.DomainEventBridge;
import com.loopers.infrastructure.event.EventType;
import com.loopers.interfaces.api.product.ProductResponse;
import com.loopers.interfaces.api.product.ProductSearchRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {

    private final ProductService productService;
    private final DomainEventBridge eventBridge;
    private final RedisTemplate<String, String> redisTemplate;

    public Page<ProductInfo> getProducts(ProductSearchRequest request, Pageable pageable, UserId userId) {
        Page<ProductInfo> pageResult = productService.getProducts(ProductSearchCommand.from(request, pageable));
        return pageResult;
    }

    @Transactional
    public ProductResponse getProduct(Long productId, UserId userId) {
        ProductInfo productInfo = productService.getProduct(productId);
        
        if (userId != null) {
            eventBridge.publishEvent(EventType.PRODUCT_VIEWED, 
                ProductViewedEvent.ofSingleView(productId, userId));
        }
        

        Long currentRank = getProductRank(productId);
        
        return ProductResponse.from(productInfo, currentRank);
    }
    
    private Long getProductRank(Long productId) {

        for (int i = 0; i < 3; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String rankingKey = "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String member = "product:" + productId;
            
            Long rank = redisTemplate.opsForZSet().reverseRank(rankingKey, member);
            if (rank != null) {
                return rank + 1;
            }
        }
        
        return null;
    }


    public Map<Long, ProductInfo> getProductInfoMap(List<Long> productIds) {
        return productService.getProductsByIds(productIds).stream()
            .collect(Collectors.toMap(
                ProductInfo::productId,
                productInfo -> productInfo
            ));
    }
}
