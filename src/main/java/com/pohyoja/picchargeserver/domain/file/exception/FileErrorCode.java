package com.pohyoja.picchargeserver.domain.file.exception;

import com.pohyoja.picchargeserver.common.exception.CustomErrorCode;
import com.pohyoja.picchargeserver.common.exception.CustomErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements CustomErrorCodeInterface {
    INVALID_FILE_EXTENSION("F001", "파일 확장자가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    EMPTY_FILE_EXCEPTION("F002", "업로드된 파일이 없거나 파일 이름이 비어 있습니다.", HttpStatus.BAD_REQUEST),
    NO_FILE_EXTENSION("F003", "파일에 확장자가 포함되어 있지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT("F004", "지원되지 않는 파일 형식입니다. (허용 형식: jpg, jpeg, png, gif, webp, heif)", HttpStatus.BAD_REQUEST),
    INVALID_FILE_NAME("F005", "파일 이름이 UUID 형식이 아닙니다.", HttpStatus.BAD_REQUEST),

    IO_EXCEPTION_ON_IMAGE_UPLOAD("I001", "이미지 업로드 중에 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PUT_OBJECT_EXCEPTION("I002", "이미지를 S3에 저장하는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IO_EXCEPTION_ON_IMAGE_DELETE("I003", "S3에서 이미지를 삭제하는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MALFORMED_URL_EXCEPTION("I004", "이미지 주소 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_ENCODING_EXCEPTION("I005", "이미지 주소의 인코딩 방식이 지원되지 않습니다.", HttpStatus.BAD_REQUEST),
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