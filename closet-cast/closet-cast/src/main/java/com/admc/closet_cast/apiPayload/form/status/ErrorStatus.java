package com.admc.closet_cast.apiPayload.form.status;

import com.admc.closet_cast.apiPayload.form.BaseCode;
import com.admc.closet_cast.apiPayload.form.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "해당 ID의 사용자가 없습니다."),
    DUPLICATED_ID(HttpStatus.MULTI_STATUS, "MEMBER4002", "이미 사용중인 ID입니다."),
    INVALID_ID_OR_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER4003", "아이디 또는 비밀번호가 일치하지 않습니다."),

    NO_DATA(HttpStatus.NOT_FOUND, "WEATHER4001", "해당 날짜의 날씨 데이터가 없습니다."),

    INVALID_CLOTHES(HttpStatus.FORBIDDEN, "CLOTH4001", "아우터, 상의, 하의 각각 최소 하나의 의상이 필요합니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .status(status)
                .build();
    }
}
