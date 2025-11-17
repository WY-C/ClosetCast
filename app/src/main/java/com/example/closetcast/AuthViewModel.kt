package com.example.closetcast

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.AuthApiService
import com.example.closetcast.api.RetrofitClient
import com.example.closetcast.api.SignInRequestDto
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

                    val loginRequest = SignInRequestDto(loginId = loginId, password = password)
                    Log.d("AuthViewModel", "RequestBody: $loginRequest")

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
                        val errorMessage = response.message ?: "알 수 없는 오류"
                        Log.w("AuthViewModel", "로그인 실패: $errorMessage")

                        withContext(Dispatchers.Main) {
                            _error.value = errorMessage
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "오류: ${e.message}"
                Log.e("AuthViewModel", errorMessage, e)
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, loginId: String, password: String, preference: String, tendencies: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                withContext(Dispatchers.IO) {
                    Log.d("AuthViewModel", "회원가입 시도: $loginId")

                    val response = RetrofitClient.authApiService.signUp(
                        SignUpRequest(name, loginId, password, preference, tendencies)
                    )

                    if (response.isSuccess) {
                        Log.d("AuthViewModel", "회원가입 성공")
                        withContext(Dispatchers.Main) {
                            _isLoading.value = false
                        }
                    } else {
                        val errorMessage = response.message ?: "회원가입 실패"
                        Log.e("AuthViewModel", errorMessage)
                        withContext(Dispatchers.Main) {
                            _error.value = errorMessage
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "회원가입 오류", e)
                _error.value = "오류: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _userInfo.value = null
        RetrofitClient.clearToken()
        Log.d("AuthViewModel", "로그아웃 완료")
    }
}
