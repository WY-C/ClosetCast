package com.admc.closet_cast.apiPayload.exception.handler;

import com.admc.closet_cast.apiPayload.exception.GeneralException;
import com.admc.closet_cast.apiPayload.form.BaseCode;

public class MemberHandler extends GeneralException {

    private final Object result;

    public MemberHandler(BaseCode code) {
        super(code);
        this.result = null;
    }

    public MemberHandler(BaseCode code, Object result) {
        super(code);
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
