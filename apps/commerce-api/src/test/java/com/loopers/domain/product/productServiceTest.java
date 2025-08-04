package com.loopers.domain.product;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.application.product.ProductServiceImpl;
import com.loopers.support.error.CoreException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("상품 ID로 상품 정보를 조회한다.")
    void getProduct_success() {
        // given
        ProductInfo info = new ProductInfo(1L, "신발", "무신사", 10000, 5);
        given(productRepository.findProductInfoById(1L)).willReturn(Optional.of(info));

        // when
        ProductInfo result = productService.getProduct(1L);

        // then
        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.productName()).isEqualTo("신발");
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID일 경우 CoreException 404 예외를 던진다.")
    void getProduct_notFound() {
        // given
        given(productRepository.findProductInfoById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(999L))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("상품을 찾을 수 없습니다.");
    }
}



