package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Preference;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record SignUpRequestDto(
        String name,
        String loginId,
        String password,
        List<Preference> preference,
        List<Tendency> tendencies
) {

    public static SignUpRequestDto of(String name, String loginId, String password, List<Preference> preference, List<Tendency> tendencies) {
        return new SignUpRequestDto(name, loginId, password, preference, tendencies);
    }
}