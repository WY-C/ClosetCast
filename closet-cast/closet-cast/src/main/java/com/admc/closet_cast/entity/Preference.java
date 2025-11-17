package com.admc.closet_cast.entity;

public enum Preference {
    MINIMAL,
    CASUAL,
    STREET,
    CLASSIC,
    DANDY,
    RETRO;

    public static Preference fromString(String value) {
        return Preference.valueOf(value.toUpperCase());
    }
}
