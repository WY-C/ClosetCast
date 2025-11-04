package com.admc.closet_cast.dto;

import com.admc.closet_cast.entity.Cloth;
import com.admc.closet_cast.entity.Tendency;

import java.util.List;

public record RecommendDto(
        String top,
        String bottom
) {
    public static RecommendDto of(String top, String bottom) {
        return new RecommendDto(top, bottom);
    }
}
