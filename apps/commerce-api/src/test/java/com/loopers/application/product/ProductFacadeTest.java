package com.loopers.application.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.event.DomainEventBridge;
import com.loopers.infrastructure.event.EventType;
import com.loopers.interfaces.api.product.ProductResponse;
import com.loopers.interfaces.api.product.ProductSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductFacade 단위 테스트")
class ProductFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private DomainEventBridge eventBridge;

    @InjectMocks
    private ProductFacade productFacade;

    @Test
    @DisplayName("개별 상품 조회 시 SINGLE 타입 ProductViewedEvent 발행")
    void getProduct_with_userId_publishes_single_view_event() {
        // given
        Long productId = 1L;
        UserId userId = UserId.of("testUser");
        ProductInfo productInfo = new ProductInfo(1L, "Product", "Brand", 1000, 5);

        when(productService.getProduct(productId)).thenReturn(productInfo);

        // when
        ProductResponse response = productFacade.getProduct(productId, userId);

        // then
        assertThat(response).isNotNull();
        
        ArgumentCaptor<ProductViewedEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewedEvent.class);
        verify(eventBridge).publishEvent(eq(EventType.PRODUCT_VIEWED),eventCaptor.capture());
        
        ProductViewedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.productId()).isEqualTo(productId);
        assertThat(capturedEvent.userId()).isEqualTo(userId.value());
        assertThat(capturedEvent.viewType()).isEqualTo("SINGLE");
        assertThat(capturedEvent.eventId()).isNotNull();
        assertThat(capturedEvent.viewedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 ID 없이 개별 상품 조회 시 이벤트 발행하지 않음")
    void getProduct_without_userId_does_not_publish_event() {
        // given
        Long productId = 1L;
        ProductInfo productInfo = new ProductInfo(1L, "Product", "Brand", 1000, 5);

        when(productService.getProduct(productId)).thenReturn(productInfo);

        // when
        ProductResponse response = productFacade.getProduct(productId, null);

        // then
        assertThat(response).isNotNull();
        verify(eventBridge, never()).publishEvent(any(), any());
    }

    @Test
    @DisplayName("상품 목록 조회 시 각 상품별로 LIST 타입 ProductViewedEvent 발행")
    void getProducts_with_userId_publishes_list_view_events() {
        // given
        UserId userId = UserId.of("testUser");
        ProductSearchRequest request = new ProductSearchRequest(0, 20, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        List<ProductInfo> products = List.of(
            new ProductInfo(1L, "Product1", "Brand", 1000, 5),
            new ProductInfo(2L, "Product2", "Brand", 2000, 3)
        );
        Page<ProductInfo> pageResult = new PageImpl<>(products, pageable, 2);

        when(productService.getProducts(any())).thenReturn(pageResult);

        // when
        Page<ProductInfo> result = productFacade.getProducts(request, pageable, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        
        ArgumentCaptor<ProductViewedEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewedEvent.class);
        verify(eventBridge, org.mockito.Mockito.times(2)).publishEvent(eq(EventType.PRODUCT_VIEWED),eventCaptor.capture());
        
        List<ProductViewedEvent> capturedEvents = eventCaptor.getAllValues();
        
        //
        assertThat(capturedEvents.get(0).productId()).isEqualTo(1L);
        assertThat(capturedEvents.get(0).userId()).isEqualTo(userId.value());
        assertThat(capturedEvents.get(0).viewType()).isEqualTo("LIST");
        
        //
        assertThat(capturedEvents.get(1).productId()).isEqualTo(2L);
        assertThat(capturedEvents.get(1).userId()).isEqualTo(userId.value());
        assertThat(capturedEvents.get(1).viewType()).isEqualTo("LIST");
    }

    @Test
    @DisplayName("사용자 ID 없이 상품 목록 조회 시 이벤트 발행하지 않음")
    void getProducts_without_userId_does_not_publish_events() {
        // given
        ProductSearchRequest request = new ProductSearchRequest(0, 20, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        List<ProductInfo> products = List.of(
            new ProductInfo(1L, "Product1", "Brand", 1000, 5)
        );
        Page<ProductInfo> pageResult = new PageImpl<>(products, pageable, 1);

        when(productService.getProducts(any())).thenReturn(pageResult);

        // when
        Page<ProductInfo> result = productFacade.getProducts(request, pageable, null);

        // then
        assertThat(result).isNotNull();
        verify(eventBridge, never()).publishEvent(any(), any());
    }
}
