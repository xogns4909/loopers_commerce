package com.loopers.application.product;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.model.Product;
import com.loopers.interfaces.api.product.ProductResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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
    public void checkAndDeduct(List<OrderItemCommand> items) {
        for (OrderItemCommand item : items) {
            Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));


            product.checkPurchasable(item.quantity());
            product.deductStock(item.quantity());

            productRepository.save(product);
        }
    }

    @Override
    public boolean existsProduct(Long productId) {
        return productRepository.existsById(productId);
    }
}

