package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Cloth;
import com.admc.closet_cast.entity.Preference;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record MemberUpdateRequestDto(
        String password,
        List<Preference> preference,
        List<Tendency> tendencies,
        List<Cloth> clothes
) {
    public static MemberUpdateRequestDto of(String password, List<Preference> preference, List<Tendency> tendencies, List<Cloth> clothes) {
        return new MemberUpdateRequestDto(password, preference, tendencies, clothes);
    }
}
