package com.loopers.domain.brand.model;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record Brand(Long id, String name) {

    public static Brand of(Long id, String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 이름은 필수입니다.");
        }
        return new Brand(id, name);
    }


}
