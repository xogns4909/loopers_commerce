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
}
