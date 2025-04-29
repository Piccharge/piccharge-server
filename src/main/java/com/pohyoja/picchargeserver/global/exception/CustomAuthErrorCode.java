package com.pohyoja.picchargeserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomAuthErrorCode implements CustomErrorCodeInterface {
    INVALID_TOKEN("AUTH001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH002", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    WRONG_TOKEN_SIGNATURE("AUTH003", "토큰의 서명이 잘못됐습니다.", HttpStatus.UNAUTHORIZED),
    EMPTY_TOKEN("AUTH004", "토큰의 Claim 이 비어있습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_VALIDATION_FAIL("AUTH005", "토큰 유효성 검사에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE("AUTH006", "유효하지 않은 토큰 타입입니다.", HttpStatus.UNAUTHORIZED),
    ;
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public CustomErrorCode getErrorCode() {
        return CustomErrorCode.builder()
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}