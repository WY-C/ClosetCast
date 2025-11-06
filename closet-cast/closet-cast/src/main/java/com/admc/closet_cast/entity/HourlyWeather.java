package com.admc.closet_cast.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hourly_weather")
public class HourlyWeather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String fcstTime;      // HHmm

    @Setter
    private Double temperature;   // 기온

    @Setter
    private Double apparentTemp;  // 체감온도

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id")
    @Setter
    private Weather weather;
}
