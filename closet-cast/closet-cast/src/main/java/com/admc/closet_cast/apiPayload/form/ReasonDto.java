package com.admc.closet_cast.apiPayload.form;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDto {

    private HttpStatus status;

    private final boolean isSuccess;
    private final String code;
    private final String message;
}
