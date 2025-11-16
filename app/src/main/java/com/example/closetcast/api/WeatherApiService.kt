package com.example.closetcast.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

// 날씨 데이터 응답 모델
data class DailyWeatherDto(
    @SerializedName("date")
    val date: String,

    @SerializedName("temperature")
    val temperature: Int,

    @SerializedName("description")
    val description: String,

    @SerializedName("humidity")
    val humidity: Int
)

data class HourlyWeatherDto(
    @SerializedName("time")
    val time: String,

    @SerializedName("temperature")
    val temperature: Int,

    @SerializedName("description")
    val description: String
)

// 메인 날씨 API 응답
data class WeatherApiResponse(
    @SerializedName("location")
    val location: String,

    @SerializedName("temperature")
    val temperature: Double,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("description")
    val description: String,

    @SerializedName("minTemp")
    val minTemp: Int,

    @SerializedName("maxTemp")
    val maxTemp: Int,

    @SerializedName("feelsLike")
    val feelsLike: Double,

    @SerializedName("daily")
    val daily: List<DailyWeatherDto>? = null,

    @SerializedName("hourly")
    val hourly: List<HourlyWeatherDto>? = null
)

interface WeatherApiService {
    // 위치 정보(위도, 경도)를 기반으로 날씨 정보 조회
    @GET("/api/weather/get")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherApiResponse
}
