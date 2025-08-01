package com.loopers.domain.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import org.springframework.data.domain.Page;

public interface ProductService {

    Page<ProductInfo> getProducts(ProductSearchCommand from);

    boolean existsProduct(Long productId);

    ProductInfo getProduct(Long productId);
}
