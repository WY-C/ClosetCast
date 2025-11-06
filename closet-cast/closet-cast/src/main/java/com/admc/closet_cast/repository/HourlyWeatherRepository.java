package com.admc.closet_cast.repository;

import com.admc.closet_cast.entity.HourlyWeather;
import com.admc.closet_cast.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HourlyWeatherRepository extends JpaRepository<HourlyWeather, Long> {
    List<HourlyWeather> findByWeather(Weather weather);
}
