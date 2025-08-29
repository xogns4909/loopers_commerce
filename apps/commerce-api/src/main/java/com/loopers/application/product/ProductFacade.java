package com.loopers.application.product;

import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.product.like.ProductLikeService;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.event.DomainEventBridge;
import com.loopers.interfaces.api.product.ProductResponse;
import com.loopers.interfaces.api.product.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final DomainEventBridge eventBridge;


    public Page<ProductInfo> getProducts(ProductSearchRequest request, Pageable pageable, UserId userId) {
        Page<ProductInfo> pageResult = productService.getProducts(ProductSearchCommand.from(request, pageable));
        

        if (userId != null) {
            pageResult.getContent().forEach(product -> 
                eventBridge.publish(ProductViewedEvent.ofListView(product.productId(), userId))
            );
        }
        
        return pageResult;
    }

    public ProductResponse getProduct(Long productId, UserId userId) {
        ProductInfo productInfo = productService.getProduct(productId);
        

        if (userId != null) {
            eventBridge.publish(ProductViewedEvent.ofSingleView(productId, userId));
        }

        return ProductResponse.from(productInfo);
    }
}
