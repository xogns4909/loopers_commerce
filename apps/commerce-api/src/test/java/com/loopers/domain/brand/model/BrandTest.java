package com.loopers.domain.brand.model;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BrandTest {

    @Test
    @DisplayName("브랜드가 정상적으로 생성된다")
    void create_brand_success() {
        // given
        Long id = 1L;
        String name = "무신사";

        // when
        Brand brand = Brand.of(id, name);

        // then
        assertThat(brand).isNotNull();
        assertThat(brand.id()).isEqualTo(id);
        assertThat(brand.name()).isEqualTo(name);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("브랜드 이름이 null 또는 공백이면 CoreException 예외가 발생한다")
    void invalid_name_should_throw_exception(String invalidName) {
        // given
        Long id = 1L;

        // when & then
        assertThatThrownBy(() -> Brand.of(id, invalidName))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreEx = (CoreException) ex;
                assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            });
    }
}
