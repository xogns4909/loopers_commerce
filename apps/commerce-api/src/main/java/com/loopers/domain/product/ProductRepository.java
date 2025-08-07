package com.loopers.domain.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.domain.product.model.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductRepository {

    Page<ProductInfo> searchByCondition(ProductSearchCommand command);

    Optional<ProductInfo> findProductInfoById(Long id);

    boolean existsById(Long productId);

    void save(Product product);

    Optional<Product> findById(Long aLong);

    Optional<Product>  findWithPessimisticLockById(Long productId);
}
