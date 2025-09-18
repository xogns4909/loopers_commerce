package com.loopers.domain.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.domain.product.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Page<ProductInfo> searchByCondition(ProductSearchCommand command);

    Optional<ProductInfo> findProductInfoById(Long id);

    boolean existsById(Long productId);

    void save(Product product);

    Optional<Product> findById(Long productId);

    Optional<Product> findWithPessimisticLockById(Long productId);
    

    List<ProductInfo> findProductInfosByIds(List<Long> productIds);
}
