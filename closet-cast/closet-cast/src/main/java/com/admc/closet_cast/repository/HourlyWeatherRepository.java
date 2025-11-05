package com.admc.closet_cast.repository;

import com.admc.closet_cast.entity.HourlyWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourlyWeatherRepository extends JpaRepository<HourlyWeather, Long> {
}
