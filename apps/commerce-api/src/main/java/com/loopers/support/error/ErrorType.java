package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "INVALID_PRICE", "유효하지 않은 가격입니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "INVALID_STOCK", "재고 수량이 유효하지 않습니다."),
    STOCK_SHORTAGE(HttpStatus.CONFLICT, "STOCK_SHORTAGE", "재고가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
