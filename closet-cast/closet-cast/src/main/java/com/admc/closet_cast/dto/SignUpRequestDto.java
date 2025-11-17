package com.admc.closet_cast.dto;

import java.util.List;

public record SignUpRequestDto(
        String name,
        String loginId,
        String password,
        List<String> preference,
        List<String> tendencies
) {

    public static SignUpRequestDto of(String name, String loginId, String password, List<String> preference, List<String> tendencies) {
        return new SignUpRequestDto(name, loginId, password, preference, tendencies);
    }
}