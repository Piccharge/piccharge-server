package com.pohyoja.picchargeserver.domain.family.exception;

import com.pohyoja.picchargeserver.common.exception.CustomErrorCode;
import com.pohyoja.picchargeserver.common.exception.CustomErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FamilyCustomErrorCode implements CustomErrorCodeInterface {
    FAMILY_NOT_FOUND("FAMILY001", "가족을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("FAMILY002", "구성원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    NOT_FAMILY_MEMBER("FAMILY003", "가족 구성원이 아닙니다", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_FAMILY("FAMILY004", "가족 구성원이 1명 이상일 경우 가족을 삭제할 수 없습니다", HttpStatus.BAD_REQUEST),
    INVITE_CODE_NOT_FOUND("FAMILY005", "초대 코드를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVITE_CODE_EXPIRED("FAMILY006", "만료된 초대 코드입니다", HttpStatus.BAD_REQUEST),
    ALREADY_FAMILY_MEMBER("FAMILY007", "이미 가족 구성원입니다", HttpStatus.BAD_REQUEST),
    INVITE_CODE_GENERATION_FAILED("FAMILY008", "초대 코드가 5번 넘게 중복 생성되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAMILY_ALREADY_FULL("FAMILY009", "가족 구성원은 6명을 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),
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