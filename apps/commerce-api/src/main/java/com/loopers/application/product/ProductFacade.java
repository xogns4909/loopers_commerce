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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {

    private final ProductService productService;
    private final DomainEventBridge eventBridge;


    public Page<ProductInfo> getProducts(ProductSearchRequest request, Pageable pageable, UserId userId) {
        Page<ProductInfo> pageResult = productService.getProducts(ProductSearchCommand.from(request, pageable));

        
        return pageResult;
    }

    @Transactional
    public ProductResponse getProduct(Long productId, UserId userId) {
        ProductInfo productInfo = productService.getProduct(productId);
        
        log.info("userId {} ",userId.getId());
        if (userId != null) {
            eventBridge.publishEvent(EventType.PRODUCT_VIEWED, 
                ProductViewedEvent.ofSingleView(productId, userId));
        }
        log.info("끄읏 ",""+userId);
        return ProductResponse.from(productInfo);
    }
}
