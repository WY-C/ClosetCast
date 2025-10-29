package com.admc.closet_cast.dto;

public record SignInRequestDto(
        String loginId,
        String password
) {
    public static SignInRequestDto of(String name, String loginId) {
        return new SignInRequestDto(name, loginId);
    }
}
