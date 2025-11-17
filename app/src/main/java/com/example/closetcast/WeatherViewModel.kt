package com.example.closetcast

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.RetrofitClient
import com.example.closetcast.api.WeatherApiService
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
                    Log.d("WeatherViewModel", "전체 URL: http://3.39.165.91:3000/api/weather/get")
                    Log.d("WeatherViewModel", "파라미터: 없음 (서버에서 자동으로 처리)")

                    val responseList = RetrofitClient.apiService.getWeather()

                    Log.d("WeatherViewModel", "========== API 응답 받음 ==========")
                    Log.d("WeatherViewModel", "응답 배열 크기: ${responseList.size}")
                    Log.d("WeatherViewModel", "응답 전체: $responseList")
                    Log.d("WeatherViewModel", "응답 JSON: ${responseList.toString()}")

                    withContext(Dispatchers.Main) {
                        if (responseList.isNotEmpty()) {
                            val response = responseList[0]
                            Log.d("WeatherViewModel", "첫 번째 응답 데이터: $response")
                            _weatherData.value = convertApiResponseToWeatherData(response)
                            _isLoading.value = false
                        } else {
                            Log.w("WeatherViewModel", "========== 응답 배열이 비어있음 ==========")
                            Log.w("WeatherViewModel", "서버에서 빈 배열을 반환함: []")
                            _error.value = "응답 데이터가 비어있습니다 (서버에서 빈 배열 반환)"
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: HttpException) {
                val errorMessage = "HTTP 오류: ${e.code()} - ${e.message()}"
                Log.e("WeatherViewModel", "HTTP Exception: $errorMessage", e)
                _error.value = errorMessage
                _isLoading.value = false
            } catch (e: java.io.IOException) {
                val errorMessage = "네트워크 오류: ${e.message}"
                Log.e("WeatherViewModel", "IOException: $errorMessage", e)
                _error.value = errorMessage
                _isLoading.value = false
            } catch (e: Exception) {
                val errorMessage = "예상치 못한 오류: ${e.message}"
                Log.e("WeatherViewModel", "Exception: $errorMessage", e)
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    private fun convertApiResponseToWeatherData(response: WeatherApiResponse): WeatherData {
        return WeatherData(
            current = CurrentWeather(
                location = "현재 위치",
                temperature = response.tax,
                apparentTemperature = response.apparentMap?.get("additionalProp1") ?: response.tax,
                weatherCondition = "Humidity: ${response.humidity}%",
                minTemp = response.tax,
                maxTemp = response.tax
            ),
            hourly = response.hourlyList?.map { hourly ->
                HourlyForecast(
                    time = hourly.fcstime,
                    temperature = hourly.temperature,
                    weatherIcon = getWeatherIcon(hourly.temperature.toString())
                )
            } ?: listOf(),
            daily = listOf()
        )
    }

    private fun getWeatherIcon(description: String): ImageVector {
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
