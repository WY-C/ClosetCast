package com.admc.closet_cast.apiPayload.exception;

import com.admc.closet_cast.apiPayload.form.BaseCode;
import com.admc.closet_cast.apiPayload.form.ReasonDto;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private BaseCode code;

    public GeneralException(BaseCode code) {
        super(code.getReason().getMessage());
        this.code = code;
    }

    public ReasonDto getErrorReason() {
        return this.code.getReason();
    }

    public ReasonDto getErrorReasonHttpStatus() {
        return this.code.getReasonHttpStatus();
    }
}
