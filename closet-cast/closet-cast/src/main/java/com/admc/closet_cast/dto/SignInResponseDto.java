package com.admc.closet_cast.dto;

public record SignInResponseDto(
        String memberId,
        String name,
        String loginId,
        String token
) {
    public static SignInResponseDto of(String memberId, String name, String loginId, String token) {
        return new SignInResponseDto(memberId, name, loginId, token);
    }
}
