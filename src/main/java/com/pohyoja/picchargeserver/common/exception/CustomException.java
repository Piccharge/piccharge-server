package com.pohyoja.picchargeserver.common.exception;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final CustomErrorCodeInterface errorCode;

    public CustomErrorCode getErrorCode() {
        return this.errorCode.getErrorCode();
    }
}
