package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Cloth;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record RecommendDto(
        String outer,
        String top,
        String bottom
) {
    public static RecommendDto of(String outer, String top, String bottom) {
        return new RecommendDto(outer, top, bottom);
    }
}
