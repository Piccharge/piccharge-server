package com.pohyoja.picchargeserver.domain.photo.exception;

import com.pohyoja.picchargeserver.common.exception.CustomErrorCode;
import com.pohyoja.picchargeserver.common.exception.CustomErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PhotoCustomErrorCode implements CustomErrorCodeInterface {
    PHOTO_NOT_FOUND("PHOTO001", "사진을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    PHOTO_ALREADY_EXISTS("PHOTO002", "이미 존재하는 사진입니다", HttpStatus.CONFLICT),
    PHOTO_UPLOAD_FAILED("PHOTO003", "사진 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    PHOTO_DELETION_FAILED("PHOTO004", "사진 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PHOTO_DATA("PHOTO005", "유효하지 않은 사진 데이터입니다", HttpStatus.BAD_REQUEST),
    NOT_PHOTO_OWNER("PHOTO006", "사진의 소유자가 아닙니다", HttpStatus.FORBIDDEN),
    INVALID_REACTION_TYPE("PHOTO007", "유효하지 않은 반응 타입입니다", HttpStatus.BAD_REQUEST),
    INVALID_REACTION_COUNT("PHOTO008", "유효하지 않은 반응 개수입니다", HttpStatus.BAD_REQUEST),
    REMOTE_STORAGE_ERROR("PHOTO009", "원격 스토리지 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("PHOTO010", "파일 크기 제한을 초과했습니다", HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_FILE_FORMAT("PHOTO011", "지원하지 않는 파일 형식입니다", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE("PHOTO012", "유효하지 않은 페이지 크기입니다", HttpStatus.BAD_REQUEST),
    INVALID_PHOTO_URL("PHOTO013", "유효하지 않은 사진 URL입니다", HttpStatus.BAD_REQUEST);

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