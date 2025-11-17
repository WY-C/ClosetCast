package com.example.closetcast

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.AuthApiService
import com.example.closetcast.api.RetrofitClient
import com.example.closetcast.api.SignInRequestDto
import com.example.closetcast.api.SignUpRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthViewModel : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userInfo = mutableStateOf<String?>(null)
    val userInfo: State<String?> = _userInfo

    fun login(loginId: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                withContext(Dispatchers.IO) {
                    Log.d("AuthViewModel", "========== 로그인 시작 ==========")
                    Log.d("AuthViewModel", "로그인 ID: $loginId")

                    // DTO Setup
                    val loginRequest = SignInRequestDto(loginId = loginId, password = password)
                    Log.d("AuthViewModel", "RequestBody: $loginRequest")

                    // DTO Network call
                    val response = RetrofitClient.authApiService.signIn(loginRequest)

                    Log.d("AuthViewModel", "========== 응답 받음 ==========")
                    Log.d("AuthViewModel", "응답 성공 여부: ${response.isSuccess}")
                    Log.d("AuthViewModel", "응답 메시지: ${response.message}")

                    if (response.isSuccess && response.result != null) {
                        val result = response.result!!
                        val token = result.token

                        Log.d("AuthViewModel", "========== 로그인 성공 ==========")
                        Log.d("AuthViewModel", "사용자: ${result.name}")
                        Log.d("AuthViewModel", "토큰: $token")

                        RetrofitClient.setToken(token)

                        withContext(Dispatchers.Main) {
                            _userInfo.value = result.name
                            _isLoggedIn.value = true
                            _isLoading.value = false
                        }
                    } else {
                        val errorMessage = response.message ?: "Error Response"
                        Log.w("AuthViewModel", "Error Response: $errorMessage")

                        withContext(Dispatchers.Main) {
                            _error.value = errorMessage
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Error during login: ${e.message}"
                Log.e("AuthViewModel", errorMessage, e)
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, loginId: String, password: String, preference: List<String>, tendencies: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 회원가입 요청 DTO 생성
                val signUpRequest = SignUpRequestDto(
                    name = name,
                    loginId = loginId,
                    password = password,
                    preference = preference,
                    tendencies = tendencies
                )

                // API 호출 및 결과 받아오기
                val response = RetrofitClient.authApiService.signUp(signUpRequest)

                if (response.isSuccess) {
                    // 성공 시, 응답에서 필요한 데이터 활용
                    val result = response.result

                    withContext(Dispatchers.Main) {
                        // 예시: 사용자 정보 업데이트 및 로그인 상태를 true로 변경
                        _userInfo.value = result.name
                        _isLoggedIn.value = true
                        _isLoading.value = false
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = response.message
                        _isLoading.value = false
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.localizedMessage
                    _isLoading.value = false
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                // 로그인 상태 초기화
                _isLoggedIn.value = false
                _userInfo.value = null
                _isLoading.value = false
                _error.value = null
            }

            // RetrofitClient에 저장된 토큰 삭제
            RetrofitClient.setToken("")

            // TokenManager를 사용하는 경우라면 아래 코드 추가 (현재 프로젝트에선 미사용 중)
            // TokenManager(context).clearTokens()
        }
    }

}
