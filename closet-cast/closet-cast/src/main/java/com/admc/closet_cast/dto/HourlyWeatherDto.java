package com.admc.closet_cast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyWeatherDto{
    private String fcstTime;      // 예보 시각 (HHmm)
    private Double temperature;   // 기온 (T1H)
    private Double apparentTemp;  // 체감온도 (WCT)
}
