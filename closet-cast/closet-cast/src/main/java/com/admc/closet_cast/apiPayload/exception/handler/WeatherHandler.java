package com.admc.closet_cast.apiPayload.exception.handler;

import com.admc.closet_cast.apiPayload.exception.GeneralException;
import com.admc.closet_cast.apiPayload.form.BaseCode;

public class WeatherHandler extends GeneralException {

    private final Object result;

    public WeatherHandler(BaseCode code) {
        super(code);
        this.result = null;
    }

    public WeatherHandler(BaseCode code, Object result) {
        super(code);
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
