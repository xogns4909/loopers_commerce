package com.loopers.domain.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.domain.product.model.Product;
import com.loopers.interfaces.api.product.ProductResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductRepository {

    Page<ProductInfo> searchByCondition(ProductSearchCommand command);

    Optional<ProductInfo> findProductInfoById(Long id);

}
