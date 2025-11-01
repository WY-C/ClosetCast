package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Cloth;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record MemberUpdateRequestDto(
        String password,
        String preference,
        List<String> tendencies,
        List<String> clothes
) {
    public static MemberUpdateRequestDto of(String password, String preference, List<String> tendencies, List<String> clothes) {
        return new MemberUpdateRequestDto(password, preference, tendencies, clothes);
    }
}
