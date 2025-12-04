package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Preference;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record SignUpResponseDto(
        String name,
        String loginId,
        String encodedPassword,
        List<Preference> preference,
        List<Tendency> tendencies,
        Long memberId
) {
    public static SignUpResponseDto of(String name, String loginId, String encodedPassword, List<Preference> preference, List<Tendency> tendencies, Long memberId) {
        return new SignUpResponseDto(name, loginId, encodedPassword, preference, tendencies, memberId);
    }
}
