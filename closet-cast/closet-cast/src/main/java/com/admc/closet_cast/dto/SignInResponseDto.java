package com.admc.closet_cast.dto;

public record SignInResponseDto(
        Long memberId,
        String name,
        String loginId,
        String token
) {
    public static SignInResponseDto of(Long memberId, String name, String loginId, String token) {
        return new SignInResponseDto(memberId, name, loginId, token);
    }
}
