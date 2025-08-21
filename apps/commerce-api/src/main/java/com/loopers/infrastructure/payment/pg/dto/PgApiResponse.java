package com.loopers.infrastructure.payment.pg.dto;

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
    
    public static <T> PgApiResponse<T> of(T data, boolean success, String message) {
        Meta meta = new Meta(
            success ? "success" : "failure",
            success ? null : "FALLBACK_ERROR",
            message
        );
        return new PgApiResponse<>(meta, data);
    }
}
