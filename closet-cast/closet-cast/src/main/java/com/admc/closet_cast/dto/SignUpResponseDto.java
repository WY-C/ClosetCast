package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record SignUpResponseDto(
        String name,
        String loginId,
        String encodedPassword,
        String preference,
        List<Tendency> tendencies
) {
    public static SignUpResponseDto of(String name, String loginId, String encodedPassword, String preference, List<Tendency> tendencies) {
        return new SignUpResponseDto(name, loginId, encodedPassword, preference, tendencies);
    }
}
