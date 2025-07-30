package com.loopers.domain.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.domain.product.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductRepository {

    Page<ProductInfo> searchByCondition(ProductSearchCommand command);
}
