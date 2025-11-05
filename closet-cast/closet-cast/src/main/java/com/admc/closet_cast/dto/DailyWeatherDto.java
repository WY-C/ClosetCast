package com.admc.closet_cast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyWeatherDto{
    private String date; // 예보 날짜 (yyyyMMdd)
    private Double tmx;  // 최고기온
    private Double tmn;  // 최저기온
    private List<HourlyWeatherDto> hourlyList = new ArrayList<>();

    // 체감온도 임시 저장 (시간별)
    private transient Map<String, Double> apparentMap = new HashMap<>();

    public DailyWeatherDto(String date) {
        this.date = date;
    }

    public void addApparentTemp(String fcstTime, Double value) {
        apparentMap.put(fcstTime, value);
    }

    public Double getApparentTemp(String fcstTime) {
        return apparentMap.get(fcstTime);
    }
}
