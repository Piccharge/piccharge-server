package com.pohyoja.picchargeserver.domain.member.exception;

import com.pohyoja.picchargeserver.common.exception.CustomErrorCode;
import com.pohyoja.picchargeserver.common.exception.CustomErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberCustomErrorCode implements CustomErrorCodeInterface {
    MEMBER_NOT_FOUND("MEMBER001", "회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MEMBER_EMAIL_NOT_FOUND("MEMBER002", "해당 이메일의 회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MEMBER_NAME_NOT_FOUND("MEMBER003", "해당 이름의 회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MEMBER_ID_NOT_FOUND("MEMBER004", "해당 ID의 회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_EXISTS("MEMBER005", "이미 존재하는 회원입니다", HttpStatus.CONFLICT),
    MISSING_PARAMETER("MEMBER006", "이메일 또는 이름 파라미터가 필요합니다", HttpStatus.BAD_REQUEST),
    INVALID_NAME_LENGTH("MEMBER007", "이름은 2자에서 12자 이내로 입력해주세요", HttpStatus.BAD_REQUEST),
    INVALID_ID_LENGTH("MEMBER008", "아이디는 Firebase uid로, 28자여야 합니다.", HttpStatus.BAD_REQUEST);

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
