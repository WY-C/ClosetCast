package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.MemberToCloth;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record MemberDto(
        Long memberId,
        String name,
        String loginId,
        String preference,
        List<Tendency> tendencies,
        List<MemberToCloth> clothes
) {
    public static MemberDto of(Long memberId, String name, String loginId, String preference, List<Tendency> tendencies, List<MemberToCloth> clothes) {
        return new MemberDto(memberId, name, loginId, preference, tendencies, clothes);
    }
}
