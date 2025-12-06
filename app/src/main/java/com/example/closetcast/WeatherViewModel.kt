package com.example.closetcast

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.DailyWeatherDto
import com.example.closetcast.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel : ViewModel() {

    private val _weatherData = mutableStateOf<WeatherData?>(null)
    val weatherData: State<WeatherData?> = _weatherData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _currentLocation = mutableStateOf<Pair<Double, Double>?>(null)

    private val _recommendation = mutableStateOf(ClothingRecommendation("None", "None", "None"))
    fun fetchWeather(latitude: Double, longitude: Double) {
        _currentLocation.value = Pair(latitude, longitude)
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherViewModel", "========== API 호출 시작 ==========")
                    val responseList = RetrofitClient.apiService.readWeather()
                    Log.d("WeatherViewModel", "========== API 응답 받음 ==========")
                    Log.d("WeatherViewModel", "응답 배열 크기: ${responseList.size}")

                    withContext(Dispatchers.Main) {
                        if (responseList.isNotEmpty()) {
                            // ✅ 현재 날짜에 해당하는 데이터 찾기
                            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            }.format(Date())
                            val todayResponse = responseList.find { it.date == todayDate }
                            val response = todayResponse ?: responseList.firstOrNull()!!

                            Log.d("WeatherViewModel", "사용하는 날짜: ${response.date}")
                            Log.d("WeatherViewModel", "Hourly 데이터 개수: ${response.hourlyList.size}")

                            // ✅ 전체 responseList와 오늘 데이터 모두 전달
                            _weatherData.value = convertApiResponseToWeatherData(responseList, response)
                            _isLoading.value = false
                        } else {
                            Log.w("WeatherViewModel", "응답 배열이 비어있음")
                            _error.value = "응답 데이터가 비어있습니다"
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: HttpException) {
                val errorMessage = "HTTP 오류: ${e.code()} - ${e.message()}"
                Log.e("WeatherViewModel", "HTTP Exception: $errorMessage", e)
                withContext(Dispatchers.Main) {
                    _error.value = errorMessage
                    _isLoading.value = false
                }
            } catch (e: java.io.IOException) {
                val errorMessage = "네트워크 오류: ${e.message}"
                Log.e("WeatherViewModel", "IOException: $errorMessage", e)
                withContext(Dispatchers.Main) {
                    _error.value = errorMessage
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = "예상치 못한 오류: ${e.message}"
                Log.e("WeatherViewModel", "Exception: $errorMessage", e)
                withContext(Dispatchers.Main) {
                    _error.value = errorMessage
                    _isLoading.value = false
                }
            }
        }
    }

    // ✅ responseList 전체와 오늘 데이터를 받아서 처리
    private fun convertApiResponseToWeatherData(
        responseList: List<DailyWeatherDto>,
        currentDayResponse: DailyWeatherDto
    ): WeatherData {
        // 한국 시간으로 현재 시각 구하기
        val currentTime = SimpleDateFormat("HHmm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }.format(Date())
        val currentHour = currentTime.toInt()

        Log.d("WeatherViewModel", "========== 데이터 변환 시작 ==========")
        Log.d("WeatherViewModel", "현재 시각 (KST): $currentTime ($currentHour)")
        Log.d("WeatherViewModel", "사용 중인 날짜: ${currentDayResponse.date}")

        // ✅ 1. 현재 시각에 가장 가까운 hourly 데이터 찾기 (현재 온도)
        val sortedHourlyList = currentDayResponse.hourlyList.sortedBy { it.fcstTime }

        val currentHourlyData = sortedHourlyList
            .filter { it.fcstTime.toInt() <= currentHour }
            .maxByOrNull { it.fcstTime.toInt() }
            ?: sortedHourlyList.firstOrNull()

        val currentTemp = currentHourlyData?.temperature ?: currentDayResponse.tmx
        val currentApparentTemp = currentHourlyData?.apparentTemp ?: currentDayResponse.tmx

        Log.d("WeatherViewModel", "선택된 현재 시각 데이터: ${currentHourlyData?.fcstTime}")
        Log.d("WeatherViewModel", "현재 온도: $currentTemp, 체감온도: $currentApparentTemp")


        // 2. Hourly
        val todayHourlyList = sortedHourlyList
            .filter { it.fcstTime.toInt() >= currentHour }
        Log.d("WeatherViewModel", "오늘 남은 Hourly 데이터: ${todayHourlyList.size}")

        val hourlyForecasts = mutableListOf<HourlyForecast>()
        todayHourlyList.forEach { hourly ->
            hourlyForecasts.add(
                HourlyForecast(
                    time = formatTime(hourly.fcstTime),
                    temperature = hourly.temperature,
                    weatherIcon = getWeatherIcon(hourly.temperature)
                )
            )
        }

        // 3. 6개 보장 (오늘 데이터 부족 시 내일 데이터로 채우기)
        if (hourlyForecasts.size < 6) {
            val needed = 6 - hourlyForecasts.size
            Log.d("WeatherViewModel", "부족한 Hourly 개수: $needed")

            val currentDateInt = currentDayResponse.date.toInt()
            val tomorrowResponse = responseList
                .sortedBy { it.date }
                .firstOrNull { it.date.toInt() > currentDateInt }

            if (tomorrowResponse != null) {
                Log.d("WeatherViewModel", "내일 날짜: ${tomorrowResponse.date}")
                val tomorrowHourlyList = tomorrowResponse.hourlyList
                    .sortedBy { it.fcstTime }
                    .take(needed)

                tomorrowHourlyList.forEach { hourly ->
                    hourlyForecasts.add(
                        HourlyForecast(
                            time = formatTime(hourly.fcstTime),
                            temperature = hourly.temperature,
                            weatherIcon = getWeatherIcon(hourly.temperature)
                        )
                    )
                }
                Log.d("WeatherViewModel", "내일에서 가져온 Hourly 개수: ${tomorrowHourlyList.size}")
            } else {
                Log.w("WeatherViewModel", "내일 데이터가 없어 Hourly 6개를 채우지 못했습니다.")
            }
        }

        // ✅ 여기서 최종 6개로 고정
        val finalHourly = hourlyForecasts.take(6)

        Log.d("WeatherViewModel", "최종 Hourly 예보 개수: ${finalHourly.size}")
        finalHourly.forEach {
            Log.d("WeatherViewModel", "D    ${it.time}: ${it.temperature}°")
        }

        // 4. Daily 3일 예보 생성
        val dailyForecasts = responseList.mapIndexed { index, dayData ->
            DailyForecast(
                day = formatDateToDay(dayData.date, index),
                minTemp = dayData.tmn,
                maxTemp = dayData.tmx,
                weatherIcon = getWeatherIconForDay(dayData.tmx)
            )
        }

        Log.d("WeatherViewModel", "Daily 예보 개수: ${dailyForecasts.size}")

        return WeatherData(
            current = CurrentWeather(
                location = "현재 위치",
                temperature = currentTemp,
                apparentTemperature = currentApparentTemp,
                weatherCondition = "날씨 정보",
                minTemp = currentDayResponse.tmn,
                maxTemp = currentDayResponse.tmx
            ),
            hourly = finalHourly,
            daily = dailyForecasts
        )
    }


    // 날짜를 요일로 변환
    // 날짜를 Today/요일로 변환
    private fun formatDateToDay(dateStr: String, index: Int): String {
        return try {
            // 오늘 날짜를 서버와 같은 yyyyMMdd 포맷으로 계산 (KST 고정)
            val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }
            val todayStr = dayFormat.format(Date())   // 예: "20251127"

            return when (dateStr) {
                todayStr -> "Today"  // 🔹 서버 date == 오늘 문자열이면 Today

                else -> {
                    // 그 외는 요일만 표시 (내일/모레 모두 Mon, Tue 등으로)
                    val parseFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    val date = parseFormat.parse(dateStr)
                    SimpleDateFormat("EEE", Locale.getDefault()).format(date!!)
                }
            }
        } catch (e: Exception) {
            "Day ${index + 1}"
        }
    }


    // 시간 포맷팅 (0500 -> "5 AM")
    private fun formatTime(fcstTime: String): String {
        return try {
            val hour = fcstTime.substring(0, 2).toInt()
            when {
                hour == 0 -> "12 AM"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "시간 포맷팅 실패: $fcstTime", e)
            fcstTime
        }
    }

    // 온도 기반 아이콘
    private fun getWeatherIcon(temperature: Double): ImageVector {
        return when {
            temperature >= 28 -> Icons.Default.WbSunny
            temperature >= 20 -> Icons.Default.WbSunny
            temperature >= 10 -> Icons.Default.Cloud
            else -> Icons.Default.AcUnit
        }
    }

    private fun getWeatherIconForDay(maxTemp: Double): ImageVector {
        return when {
            maxTemp >= 25 -> Icons.Default.WbSunny
            maxTemp >= 15 -> Icons.Default.Cloud
            else -> Icons.Default.AcUnit
        }
    }

    fun getRecommend(memberId: Long, onSuccess: (ClothingRecommendation) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getRecommend(memberId)
                }
                if (response.isSuccess) {
                    val result = ClothingRecommendation(
                        outer = response.result.outer,
                        top = response.result.top,
                        bottom = response.result.bottom
                    )
                    _recommendation.value = result
                    withContext(Dispatchers.Main) {
                        onSuccess(result)
                    }
                } else {
                    Log.e("WeatherViewModel", "API Error: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception in getRecommend: ${e.message}", e)
            }
        }
    }

}
