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

class WeatherViewModel : ViewModel() {
    private val _weatherData = mutableStateOf<WeatherData?>(null)
    val weatherData: State<WeatherData?> = _weatherData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _currentLocation = mutableStateOf<Pair<Double, Double>?>(null)
    val currentLocation: State<Pair<Double, Double>?> = _currentLocation

    fun fetchWeather(latitude: Double, longitude: Double) {
        _currentLocation.value = Pair(latitude, longitude)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherViewModel", "========== API 호출 시작 ==========")
                    Log.d("WeatherViewModel", "Endpoint: /api/weather/get")
                    Log.d("WeatherViewModel", "Base URL: http://3.39.165.91:3000/")

                    // ✅ 수정: RetrofitClient.apiService 사용
                    val responseList = RetrofitClient.apiService.readWeather()

                    Log.d("WeatherViewModel", "========== API 응답 받음 ==========")
                    Log.d("WeatherViewModel", "응답 배열 크기: ${responseList.size}")

                    withContext(Dispatchers.Main) {
                        if (responseList.isNotEmpty()) {
                            val response = responseList[0]
                            Log.d("WeatherViewModel", "첫 번째 응답 데이터: $response")
                            _weatherData.value = convertApiResponseToWeatherData(response)
                            _isLoading.value = false
                        } else {
                            Log.w("WeatherViewModel", "========== 응답 배열이 비어있음 ==========")
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

    // ✅ 수정: DailyWeatherDto를 WeatherData로 변환
    private fun convertApiResponseToWeatherData(response: DailyWeatherDto): WeatherData {
        return WeatherData(
            current = CurrentWeather(
                location = "현재 위치",
                temperature = response.tmx, // ✅ tax → tmx로 수정
                apparentTemperature = response.apparentMap.values.firstOrNull() ?: response.tmx,
                weatherCondition = "날씨 정보",
                minTemp = response.tmn, // ✅ 최저 온도
                maxTemp = response.tmx  // ✅ 최고 온도
            ),
            hourly = response.hourlyList.map { hourly ->
                HourlyForecast(
                    time = hourly.fcstTime, // ✅ fcstime → fcstTime으로 수정
                    temperature = hourly.temperature,
                    weatherIcon = readWeatherIcon(hourly.temperature.toString())
                )
            },
            daily = listOf() // 일별 예보는 현재 API에서 제공되지 않음
        )
    }

    private fun readWeatherIcon(description: String): ImageVector {
        return when {
            description.contains("sunny", ignoreCase = true) ||
                    description.contains("clear", ignoreCase = true) -> Icons.Default.WbSunny
            description.contains("cloudy", ignoreCase = true) ||
                    description.contains("cloud", ignoreCase = true) -> Icons.Default.Cloud
            description.contains("rain", ignoreCase = true) -> Icons.Default.Grain
            description.contains("snow", ignoreCase = true) -> Icons.Default.AcUnit
            else -> Icons.Default.WbSunny
        }
    }
}
