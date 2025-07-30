package com.loopers.domain.product;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.application.product.ProductServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    public class SortCondition {

        @Test
        public void sortByLatest() {
            ProductSearchCommand command = new ProductSearchCommand(
                PageRequest.of(0, 10),
                ProductSortType.LATEST,
                null
            );

            List<ProductInfo> resultData = List.of(
                new ProductInfo(10L, "Latest", "BrandA", 10000, 0),
                new ProductInfo(5L, "Older", "BrandA", 8000, 0)
            );

            given(productRepository.searchByCondition(command))
                .willReturn(new PageImpl<>(resultData));

            Page<ProductInfo> result = productService.getProducts(command);

            assertThat(result.getContent()).extracting(ProductInfo::productId)
                .containsExactly(10L, 5L);
        }

        @Test
        public void sortByPriceDesc() {
            ProductSearchCommand command = new ProductSearchCommand(
                PageRequest.of(0, 10),
                ProductSortType.PRICE_DESC,
                null
            );

            List<ProductInfo> resultData = List.of(
                new ProductInfo(2L, "Expensive", "BrandA", 20000, 0),
                new ProductInfo(1L, "Cheap", "BrandA", 10000, 0)
            );

            given(productRepository.searchByCondition(command))
                .willReturn(new PageImpl<>(resultData));

            Page<ProductInfo> result = productService.getProducts(command);

            assertThat(result.getContent()).extracting(ProductInfo::price)
                .containsExactly(20000, 10000);
        }

        @Test
        public void sortByLikesDesc() {
            ProductSearchCommand command = new ProductSearchCommand(
                PageRequest.of(0, 10),
                ProductSortType.LIKES_DESC,
                null
            );

            List<ProductInfo> resultData = List.of(
                new ProductInfo(3L, "Popular", "BrandA", 15000, 12),
                new ProductInfo(4L, "Less popular", "BrandA", 15000, 5)
            );

            given(productRepository.searchByCondition(command))
                .willReturn(new PageImpl<>(resultData));

            Page<ProductInfo> result = productService.getProducts(command);

            assertThat(result.getContent()).extracting(ProductInfo::likeCount)
                .containsExactly(12, 5);
        }
    }
}



