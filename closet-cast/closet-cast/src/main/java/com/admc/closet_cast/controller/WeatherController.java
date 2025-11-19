package com.admc.closet_cast.controller;

import com.admc.closet_cast.dto.DailyWeatherDto;
import com.admc.closet_cast.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
@EnableScheduling
public class WeatherController {

    private final WeatherService weatherService;
    private static final int NX = 55;
    private static final int NY = 127;

    @Operation(summary = "날씨 정보 저장", description = "정해진 시간마다 기상청으로부터 날씨 정보를 받아옵니다.")
    @Scheduled(cron = "0 30 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    @GetMapping("/get")
    public ResponseEntity<List<DailyWeatherDto>> getWeather() {
        // 현재 시각 기준 (예: 05:30이라면 time=0500)
        LocalDateTime now = LocalDateTime.now().minusMinutes(30);

        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = now.format(DateTimeFormatter.ofPattern("HHmm"));
        return ResponseEntity.ok(weatherService.getForecast(date, time, NX, NY));
    }

    @Operation(summary = "날씨 정보 불러오기", description = "최근 3일 간의 날씨 정보를 불러옵니다.")
    @GetMapping("/read")
    public ResponseEntity<List<DailyWeatherDto>> readWeather() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return ResponseEntity.ok(weatherService.getDailyWeather(date));
    }
}
