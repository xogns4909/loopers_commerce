package com.loopers.application.product;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Product;
import com.loopers.support.annotation.HandleConcurrency;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductInfo> getProducts(ProductSearchCommand command) {
        return productRepository.searchByCondition(command);
    }

    @Override
    public ProductInfo getProduct(Long productId) {
        return productRepository.findProductInfoById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    @Override
    @HandleConcurrency
    @Transactional
    public void checkAndDeduct(List<OrderItemCommand> items) {
        for (OrderItemCommand item : items) {
            Product product = productRepository.findWithPessimisticLockById(item.productId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

            product.checkPurchasable(item.quantity());
            Product deductedProduct = product.deductStock(item.quantity());

            productRepository.save(deductedProduct);
        }
    }

    @Override
    @Transactional
    public void restoreStock(Long productId, int quantity) {
        log.info("재고 복원 시작 - productId: {}, quantity: {}", productId, quantity);
        
        Product product = productRepository.findWithPessimisticLockById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        
        Product restoredProduct = product.restoreStock(quantity);
        productRepository.save(restoredProduct);
        
        log.info("재고 복원 완료 - productId: {}, quantity: {}", productId, quantity);
    }

    @Override
    public boolean existsProduct(Long productId) {
        return productRepository.existsById(productId);
    }
}
