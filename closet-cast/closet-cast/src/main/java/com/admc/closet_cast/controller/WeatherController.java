package com.admc.closet_cast.controller;

import com.admc.closet_cast.dto.DailyWeatherDto;
import com.admc.closet_cast.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/get")
    public ResponseEntity<List<DailyWeatherDto>> getWeather(
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int nx,
            @RequestParam int ny) {
        return ResponseEntity.ok(weatherService.getForecast(date, time, nx, ny));
    }
}
