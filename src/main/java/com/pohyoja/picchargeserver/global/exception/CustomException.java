package com.pohyoja.picchargeserver.global.exception;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final CustomErrorCodeInterface errorCode;

    public CustomErrorCode getErrorCode() {
        return this.errorCode.getErrorCode();
    }
}
