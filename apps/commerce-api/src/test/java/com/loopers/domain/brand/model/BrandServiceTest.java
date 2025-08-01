package com.loopers.domain.brand.model;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.loopers.application.brand.BrandServiceImpl;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    @Test
    @DisplayName("존재하는 브랜드 ID로 조회 시 Brand를 반환한다.")
    void findBrand_success() {
        // given
        Long brandId = 1L;
        Brand mockBrand = new Brand(brandId, "무신사");
        given(brandRepository.findById(brandId)).willReturn(Optional.of(mockBrand));

        // when
        Brand found = brandService.findByBrandId(brandId);

        // then
        assertThat(found).isNotNull();
        assertThat(found.name()).isEqualTo("무신사");
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 조회 시 CoreException이 발생한다.")
    void findBrand_fail() {
        // given
        Long brandId = 99L;
        given(brandRepository.findById(brandId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.findByBrandId(brandId))
            .isInstanceOf(CoreException.class);
    }
}
