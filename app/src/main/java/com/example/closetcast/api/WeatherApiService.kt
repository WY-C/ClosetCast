package com.example.closetcast.api

import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

// ===== 시간별 날씨 업데이트 =====
data class DailyWeatherDto(
    @SerializedName("date")
    val date: String,

    @SerializedName("tmx") // 오늘 최고 온도
    val tmx: Double,

    @SerializedName("tmn") // 오늘 최저 온도
    val tmn: Double,

    @SerializedName("hourlyList") // 오늘 시간 별 온도, 체감 온도
    val hourlyList: List<HourlyWeatherDto>,

    @SerializedName("apparentMap") // 시간별 체감 온도 배열
    val apparentMap: Map<String, Double>
)
data class HourlyWeatherDto(
    @SerializedName("fcstTime")
    val fcstTime: String,

    @SerializedName("temperature")
    val temperature: Double,

    @SerializedName("apparentTemp")
    val apparentTemp: Double
)

// ===== 옷 코디 추천 요청 =====
data class RecommendDto(
    @SerializedName("outer")
    val outer: String,

    @SerializedName("top")
    val top: String,

    @SerializedName("bottom")
    val bottom: String,
)

data class ApiResponseRecommendDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: RecommendDto
)

interface WeatherApiService {
    @GET("/api/weather/read")
    suspend fun readWeather(): List<DailyWeatherDto>
    @GET("/api/weather/get")
    suspend fun getWeather(): List<DailyWeatherDto>

    @GET("/api/recommend/{memberId}")
    suspend fun getRecommend(@Path("memberId") memberId: Long): ApiResponseRecommendDto
}
