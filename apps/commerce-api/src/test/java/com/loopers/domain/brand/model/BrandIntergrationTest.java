package com.loopers.domain.brand.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import com.loopers.domain.brand.BrandRepository;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class BrandIntegrationTest {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Brand savedBrand;
    

    @BeforeEach
    void setUp() {
        // given
        Brand brand = new Brand(1L,"무신사");  // 혹은 빌더 사용
        savedBrand = brandRepository.save(brand);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("브랜드 단건 조회 성공")
    void success_select_brand() {
        // when
        Brand foundBrand = brandRepository.findById(savedBrand.id())
            .orElseThrow(() -> new IllegalArgumentException("브랜드 없음"));

        // then
        assertThat(foundBrand.name()).isEqualTo("무신사");
    }
}

