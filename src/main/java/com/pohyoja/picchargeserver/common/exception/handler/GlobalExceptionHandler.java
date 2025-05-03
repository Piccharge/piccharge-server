package com.pohyoja.picchargeserver.common.exception.handler;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.exception.CustomErrorCode;
import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.common.exception.global.GlobalCustomErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import javax.naming.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * CustomException 을 처리하는 핸들러
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<String>> handleCustomException(CustomException e) {
        return createResponseEntity(e.getErrorCode());
    }

    /**
     * 일반적인 예외를 처리하는 핸들러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGeneralException(Exception e) {
        log.error("Unhandled Exception 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.INTERNAL_SERVER_ERROR.getErrorCode());
    }

    /**
     * ConstraintViolationException 예외 처리 (Validation 실패)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        log.error("ConstraintViolationException 빌생", e);
        return createResponseEntity(GlobalCustomErrorCode.VALIDATION_FAILED.getErrorCode());
    }

    /**
     * MethodArgumentTypeMismatchException 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        log.error("ConstraintViolationException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.getErrorCode());
    }

    /**
     * MethodArgumentNotValidException 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        log.error("MethodArgumentNotValidException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.METHOD_ARGUMENT_NOT_VALID.getErrorCode());
    }

    /**
     * NoHandlerFoundException 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleNoHandlerFoundException(
            NoHandlerFoundException e
    ) {
        log.error("NoHandlerFoundException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.NO_HANDLER_FOUND.getErrorCode());
    }

    /**
     * DataIntegrityViolationException 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ) {
        log.error("DataIntegrityViolationException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.DATA_INTEGRITY_VIOLATION.getErrorCode());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseResponse<String>> handleDataAccessException(DataAccessException e) {
        log.error("DataAccessException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.DATA_ACCESS_ERROR.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<String>> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.AUTHENTICATION_FAILED.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.ACCESS_DENIED.getErrorCode());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("HttpMediaTypeNotSupportedException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getErrorCode());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpClientErrorException(HttpClientErrorException e) {
        log.error("HttpClientErrorException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.CLIENT_ERROR.getErrorCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpServerErrorException(HttpServerErrorException e) {
        log.error("HttpServerErrorException 발생", e);
        return createResponseEntity(GlobalCustomErrorCode.SERVER_ERROR.getErrorCode());
    }

    private ResponseEntity<BaseResponse<String>> createResponseEntity(CustomErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        null));
    }
}