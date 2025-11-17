package com.example.closetcast.api

import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

// 시간별 날씨 데이터
data class HourlyWeatherDto(
    @SerializedName("fcstime")
    val fcstime: String,

    @SerializedName("temperature")
    val temperature: Int,

    @SerializedName("apparentTemp")
    val apparentTemp: Int
)

// API 응답 모델
data class WeatherApiResponse(
    @SerializedName("date")
    val date: String,

    @SerializedName("tax")
    val tax: Int,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("hourlyList")
    val hourlyList: List<HourlyWeatherDto>? = null,

    @SerializedName("apparentMap")
    val apparentMap: Map<String, Int>? = null
)

interface WeatherApiService {
    @GET("/api/weather/get")
    suspend fun getWeather(): List<WeatherApiResponse>  // ← 배열로 변경!
}
