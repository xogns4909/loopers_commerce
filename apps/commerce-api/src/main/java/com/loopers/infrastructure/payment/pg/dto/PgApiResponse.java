package com.loopers.infrastructure.payment.pg.dto;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record PgApiResponse<T>(
    Meta meta,
    T data
) {
    public record Meta(
        String result,
        String errorCode,
        String message
    ) {}
    
    public boolean success() {
        return meta != null && "success".equals(meta.result());
    }
    
    public String message() {
        return meta != null ? meta.message() : "Unknown error";
    }
    
    /**
     * 응답 검증 후 데이터 반환
     * @return 검증된 데이터
     * @throws CoreException 검증 실패 시
     */
    public T getValidatedData() {
        validateResponse();
        validateSuccess();
        validateData();
        return data;
    }
    
    private void validateResponse() {
        if (meta == null) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 메타 정보 없음");
        }
    }
    
    private void validateSuccess() {
        if (!"success".equals(meta.result())) {
            String msg = meta.message() != null ? meta.message() : "PG 실패(원인 불명)";
            throw new CoreException(ErrorType.INTERNAL_ERROR, msg);
        }
    }
    
    private void validateData() {
        if (data == null) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답 데이터 없음");
        }
    }
    
    public static <T> PgApiResponse<T> of(T data, boolean success, String message) {
        Meta meta = new Meta(
            success ? "success" : "failure",
            success ? null : "FALLBACK_ERROR",
            message
        );
        return new PgApiResponse<>(meta, data);
    }
}
