package com.admc.closet_cast.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "weather")
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String date;   // yyyyMMdd

    @Setter
    private Double tmx;    // 최고기온

    @Setter
    private Double tmn;    // 최저기온

    @OneToMany(mappedBy = "weather", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyWeather> hourlyList = new ArrayList<>();

    public void addHourly(HourlyWeather hour) {
        hourlyList.add(hour);
        hour.setWeather(this);
    }
}
