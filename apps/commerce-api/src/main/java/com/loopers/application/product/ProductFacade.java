package com.loopers.application.product;




import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.product.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;


    public Page<ProductInfo> getProducts(ProductSearchRequest request, Pageable pageable) {
        return productService.getProducts(ProductSearchCommand.from(request,pageable));
    }

}
